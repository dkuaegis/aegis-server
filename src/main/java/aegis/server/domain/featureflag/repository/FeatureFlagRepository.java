package aegis.server.domain.featureflag.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.featureflag.domain.FeatureFlag;
import aegis.server.domain.featureflag.domain.FeatureFlagKey;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    Optional<FeatureFlag> findByFeatureKey(FeatureFlagKey featureKey);

    List<FeatureFlag> findAllByFeatureKeyIn(Collection<FeatureFlagKey> featureKeys);
}
