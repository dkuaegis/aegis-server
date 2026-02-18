package aegis.server.domain.featureflag.service;

import java.time.LocalDateTime;

import aegis.server.domain.featureflag.domain.FeatureFlag;
import aegis.server.domain.featureflag.domain.FeatureFlagKey;
import aegis.server.domain.featureflag.domain.FeatureFlagValueType;

public record FeatureFlagSnapshot(
        Long id,
        FeatureFlagKey featureKey,
        FeatureFlagValueType valueType,
        String value,
        String description,
        LocalDateTime updatedAt) {

    public static FeatureFlagSnapshot from(FeatureFlag featureFlag) {
        return new FeatureFlagSnapshot(
                featureFlag.getId(),
                featureFlag.getFeatureKey(),
                featureFlag.getValueType(),
                featureFlag.getValue(),
                featureFlag.getDescription(),
                featureFlag.getUpdatedAt());
    }
}
