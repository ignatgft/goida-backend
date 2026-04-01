package ru.goidaai.test_backend;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.stereotype.Component;
import ru.goidaai.test_backend.repository.ReceiptRepository;
import ru.goidaai.test_backend.repository.TransactionRepository;
import ru.goidaai.test_backend.repository.UserRepository;
import ru.goidaai.test_backend.security.GoogleTokenVerifier;
import ru.goidaai.test_backend.security.GoogleUser;
import ru.goidaai.test_backend.service.ReceiptOcrProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private StubGoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private StubReceiptOcrProvider receiptOcrProvider;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    void googleLoginCreatesUserAndReturnsJwt() throws Exception {
        googleTokenVerifier.setGoogleUser(
            new GoogleUser("sub-1", "user1@goida.ai", "User One", "https://cdn.example/avatar.png", true)
        );

        mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idToken": "google-id-token",
                      "accessToken": "google-access-token"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.sessionToken").isNotEmpty())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("user1@goida.ai"))
            .andExpect(jsonPath("$.user.fullName").value("User One"));
    }

    @Test
    void googleLoginFallsBackToClientProfileWhenIdTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "googleId": "android-google-subject",
                      "email": "android@goida.ai",
                      "displayName": "Android User",
                      "photoUrl": "https://cdn.example/android.png"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.sessionToken").isNotEmpty())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("android@goida.ai"))
            .andExpect(jsonPath("$.user.fullName").value("Android User"));
    }

    @Test
    void dashboardReturnsEmptyStateForNewUser() throws Exception {
        String token = authenticate("sub-empty", "empty@goida.ai", "Empty User");

        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("empty@goida.ai"))
            .andExpect(jsonPath("$.baseCurrency").value("USD"));

        mockMvc.perform(get("/api/dashboard/overview")
                .param("period", "month")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.baseCurrency").value("USD"))
            .andExpect(jsonPath("$.periodLabel").value("Last 30 days"))
            .andExpect(jsonPath("$.assets").isArray())
            .andExpect(jsonPath("$.assets").isEmpty())
            .andExpect(jsonPath("$.spending.spent").value(0))
            .andExpect(jsonPath("$.spending.budget").value(0));
    }

    @Test
    void assetCrudWorksForAuthenticatedUser() throws Exception {
        String token = authenticate("sub-asset", "asset@goida.ai", "Asset User");

        MvcResult createResult = mockMvc.perform(post("/api/assets")
                .with(csrf())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Cash Wallet",
                      "type": "bank_account",
                      "currency": "USD",
                      "amount": 1200.00,
                      "note": "Primary wallet"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Cash Wallet"))
            .andExpect(jsonPath("$.type").value("bank_account"))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.amount").value(1200.00))
            .andExpect(jsonPath("$.currentValue").value(1200.00))
            .andReturn();

        String assetId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/dashboard/overview")
                .param("period", "month")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets[0].id").value(assetId))
            .andExpect(jsonPath("$.assets[0].currency").value("USD"))
            .andExpect(jsonPath("$.assets[0].amount").value(1200.00))
            .andExpect(jsonPath("$.assets[0].currentValue").value(1200.00));

        mockMvc.perform(put("/api/assets/{assetId}", assetId)
                .with(csrf())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Cash Wallet",
                      "type": "bank_account",
                      "currency": "USD",
                      "amount": 1500.00,
                      "note": "Updated wallet"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(1500.00))
            .andExpect(jsonPath("$.note").value("Updated wallet"));

        mockMvc.perform(delete("/api/assets/{assetId}", assetId)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void createTransactionWithoutAssetPersistsReceiptMetadata() throws Exception {
        String token = authenticate("sub-tx", "tx@goida.ai", "Tx User");

        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Whole Foods",
                      "category": "groceries",
                      "type": "expense",
                      "amount": 24.50,
                      "currency": "USD",
                      "createdAt": "2026-03-27T10:10:00Z",
                      "note": "Whole Foods",
                      "receipt": {
                        "merchant": "Whole Foods",
                        "total": 24.50,
                        "currency": "USD",
                        "purchasedAt": "2026-03-27T10:10:00Z",
                        "items": [
                          {
                            "name": "Milk",
                            "quantity": 1,
                            "unitPrice": 4.50,
                            "totalPrice": 4.50
                          }
                        ]
                      }
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Whole Foods"))
            .andExpect(jsonPath("$.category").value("groceries"))
            .andExpect(jsonPath("$.type").value("expense"))
            .andExpect(jsonPath("$.sourceAssetId").isEmpty())
            .andExpect(jsonPath("$.createdAt").value("2026-03-27T10:10:00Z"))
            .andExpect(jsonPath("$.receipt.merchant").value("Whole Foods"))
            .andExpect(jsonPath("$.receipt.id").isNotEmpty())
            .andExpect(jsonPath("$.receipt.items[0].title").value("Milk"))
            .andExpect(jsonPath("$.receipt.items[0].price").value(4.50))
            .andExpect(jsonPath("$.receipt.items[0].name").value("Milk"));

        org.assertj.core.api.Assertions.assertThat(transactionRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(receiptRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(receiptRepository.findAll().get(0).getTransaction()).isNotNull();
    }

    @Test
    void transactionsSupportCategoryFilterAndCursorPagination() throws Exception {
        String token = authenticate("sub-page", "page@goida.ai", "Page User");

        createTransaction(token, "groceries", "2026-03-27T10:00:00Z");
        createTransaction(token, "travel", "2026-03-27T11:00:00Z");
        createTransaction(token, "groceries", "2026-03-27T12:00:00Z");

        MvcResult firstPage = mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .param("category", "groceries")
                .param("period", "all")
                .param("limit", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].category").value("groceries"))
            .andExpect(jsonPath("$.hasMore").value(true))
            .andReturn();

        String nextCursor = objectMapper.readTree(firstPage.getResponse().getContentAsString()).get("nextCursor").asText();

        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .param("category", "groceries")
                .param("period", "all")
                .param("limit", "1")
                .param("cursor", nextCursor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].category").value("groceries"));
    }

    @Test
    void receiptProcessingDoesNotCreateTransaction() throws Exception {
        String token = authenticate("sub-receipt", "receipt@goida.ai", "Receipt User");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "receipt.png",
            "image/png",
            "png-data".getBytes()
        );

        mockMvc.perform(multipart("/api/receipt/process")
                .file(file)
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.merchant").value("Mega Store"))
            .andExpect(jsonPath("$.total").value(19.90))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.items[0].title").value("Coffee"))
            .andExpect(jsonPath("$.items[0].price").value(19.90))
            .andExpect(jsonPath("$.items[0].name").value("Coffee"));

        org.assertj.core.api.Assertions.assertThat(transactionRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(receiptRepository.count()).isEqualTo(1);
    }

    @Test
    void quickActionsCreateTransferAndTopUpTransactions() throws Exception {
        String token = authenticate("sub-quick", "quick@goida.ai", "Quick User");

        mockMvc.perform(post("/api/topup")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "amount": 250.00
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Top up"))
            .andExpect(jsonPath("$.type").value("income"))
            .andExpect(jsonPath("$.category").value("transfer"))
            .andExpect(jsonPath("$.amount").value(250.00));

        mockMvc.perform(post("/api/send")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recipient": "Alex",
                      "amount": 75.50
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Transfer to Alex"))
            .andExpect(jsonPath("$.type").value("transfer"))
            .andExpect(jsonPath("$.category").value("transfer"))
            .andExpect(jsonPath("$.note").value("Recipient: Alex"));
    }

    private String authenticate(String subject, String email, String fullName) throws Exception {
        googleTokenVerifier.setGoogleUser(
            new GoogleUser(subject, email, fullName, "https://cdn.example/avatar.png", true)
        );

        MvcResult result = mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idToken": "token",
                      "accessToken": "access"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private void createTransaction(String token, String category, String occurredAt) throws Exception {
        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "%s transaction",
                      "category": "%s",
                      "type": "expense",
                      "amount": 15.00,
                      "currency": "USD",
                      "createdAt": "%s"
                    }
                    """.formatted(category, category, occurredAt)))
            .andExpect(status().isCreated());
    }

    @org.springframework.context.annotation.Primary
    @org.springframework.stereotype.Component
    static class StubGoogleTokenVerifier implements GoogleTokenVerifier {

        private GoogleUser googleUser = new GoogleUser(
            "default-subject",
            "default@goida.ai",
            "Default User",
            "https://cdn.example/default.png",
            true
        );

        void setGoogleUser(GoogleUser googleUser) {
            this.googleUser = googleUser;
        }

        @Override
        public GoogleUser verify(String idToken, String accessToken) {
            return googleUser;
        }
    }

    @Component
    @org.springframework.context.annotation.Primary
    static class StubReceiptOcrProvider implements ReceiptOcrProvider {

        private ReceiptExtractionResult result;

        void setResult(ReceiptExtractionResult result) {
            this.result = result;
        }

        @Override
        public ReceiptExtractionResult extract(org.springframework.web.multipart.MultipartFile multipartFile) {
            return result != null ? result : new ReceiptExtractionResult(
                "Test Store",
                new BigDecimal("10.00"),
                "USD",
                Instant.now(),
                List.of(),
                "mock"
            );
        }
    }
}
