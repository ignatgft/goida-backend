package ru.goidaai.test_backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.goidaai.test_backend.model.Asset;

public interface AssetRepository extends JpaRepository<Asset, String> {

    List<Asset> findByUser_IdOrderByCreatedAtDesc(String userId);

    java.util.Optional<Asset> findByIdAndUser_Id(String assetId, String userId);
}
