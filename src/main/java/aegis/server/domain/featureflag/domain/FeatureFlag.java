package aegis.server.domain.featureflag.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Table(
        name = "feature_flag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"feature_key"}),
        indexes = @Index(name = "idx_feature_flag_feature_key", columnList = "feature_key"))
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeatureFlag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_flag_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeatureFlagKey featureKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeatureFlagValueType valueType;

    @Column(name = "\"value\"", nullable = false)
    private String value;

    private String description;

    public static FeatureFlag create(
            FeatureFlagKey featureKey, FeatureFlagValueType valueType, String value, String description) {
        return FeatureFlag.builder()
                .featureKey(featureKey)
                .valueType(valueType)
                .value(value)
                .description(description)
                .build();
    }

    public void update(FeatureFlagValueType valueType, String value, String description) {
        this.valueType = valueType;
        this.value = value;
        this.description = description;
    }
}
