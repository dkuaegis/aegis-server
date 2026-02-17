package aegis.server.domain.featureflag.service;

import aegis.server.domain.featureflag.domain.FeatureFlagKey;
import aegis.server.domain.featureflag.domain.FeatureFlagValueType;

public record FeatureFlagUpsertCommand(
        FeatureFlagKey featureKey, FeatureFlagValueType valueType, String value, String description) {

    public static FeatureFlagUpsertCommand of(
            FeatureFlagKey featureKey, FeatureFlagValueType valueType, String value, String description) {
        return new FeatureFlagUpsertCommand(featureKey, valueType, value, description);
    }
}
