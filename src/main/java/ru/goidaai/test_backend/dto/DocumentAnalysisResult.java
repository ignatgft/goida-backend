package ru.goidaai.test_backend.dto;

import java.util.Map;

public class DocumentAnalysisResult {

    private String documentType;
    private String extractedText;
    private Map<String, Object> extractedData;
    private Boolean isExpenseOrIncome;
    private String suggestedCategory;
    private Double suggestedAmount;
    private String suggestedDate;
    private String description;
    private Boolean shouldAddToSystem;

    public DocumentAnalysisResult() {
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Map<String, Object> getExtractedData() {
        return extractedData;
    }

    public void setExtractedData(Map<String, Object> extractedData) {
        this.extractedData = extractedData;
    }

    public Boolean getIsExpenseOrIncome() {
        return isExpenseOrIncome;
    }

    public void setIsExpenseOrIncome(Boolean isExpenseOrIncome) {
        this.isExpenseOrIncome = isExpenseOrIncome;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public Double getSuggestedAmount() {
        return suggestedAmount;
    }

    public void setSuggestedAmount(Double suggestedAmount) {
        this.suggestedAmount = suggestedAmount;
    }

    public String getSuggestedDate() {
        return suggestedDate;
    }

    public void setSuggestedDate(String suggestedDate) {
        this.suggestedDate = suggestedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getShouldAddToSystem() {
        return shouldAddToSystem;
    }

    public void setShouldAddToSystem(Boolean shouldAddToSystem) {
        this.shouldAddToSystem = shouldAddToSystem;
    }
}
