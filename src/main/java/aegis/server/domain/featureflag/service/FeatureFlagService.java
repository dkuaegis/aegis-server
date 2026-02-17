package aegis.server.domain.featureflag.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.domain.FeatureFlag;
import aegis.server.domain.featureflag.domain.FeatureFlagKey;
import aegis.server.domain.featureflag.repository.FeatureFlagRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final Map<FeatureFlagKey, FeatureFlagSnapshot> cache = new ConcurrentHashMap<>();

    @PostConstruct
    void initializeCache() {
        reloadCacheFromDatabase();
    }

    public Optional<FeatureFlagSnapshot> findCachedFlag(FeatureFlagKey featureKey) {
        return Optional.ofNullable(cache.get(featureKey));
    }

    public List<FeatureFlagSnapshot> findAllCachedFlags() {
        return cache.values().stream()
                .sorted(Comparator.comparing(FeatureFlagSnapshot::featureKey))
                .toList();
    }

    public void reloadCacheFromDatabase() {
        Map<FeatureFlagKey, FeatureFlagSnapshot> loaded = featureFlagRepository.findAll().stream()
                .map(FeatureFlagSnapshot::from)
                .collect(Collectors.toMap(FeatureFlagSnapshot::featureKey, Function.identity()));

        cache.clear();
        cache.putAll(loaded);
    }

    @Transactional
    public List<FeatureFlagSnapshot> upsertAll(List<FeatureFlagUpsertCommand> commands) {
        if (commands.isEmpty()) {
            return List.of();
        }

        Set<FeatureFlagKey> targetKeys =
                commands.stream().map(FeatureFlagUpsertCommand::featureKey).collect(Collectors.toSet());

        Map<FeatureFlagKey, FeatureFlag> existingMap = featureFlagRepository.findAllByFeatureKeyIn(targetKeys).stream()
                .collect(Collectors.toMap(FeatureFlag::getFeatureKey, Function.identity()));

        List<FeatureFlag> flagsToSave = commands.stream()
                .map(command -> {
                    FeatureFlag existing = existingMap.get(command.featureKey());
                    if (existing == null) {
                        return FeatureFlag.create(
                                command.featureKey(), command.valueType(), command.value(), command.description());
                    }

                    existing.update(command.valueType(), command.value(), command.description());
                    return existing;
                })
                .toList();

        List<FeatureFlagSnapshot> updatedSnapshots = featureFlagRepository.saveAllAndFlush(flagsToSave).stream()
                .map(FeatureFlagSnapshot::from)
                .toList();

        Map<FeatureFlagKey, FeatureFlagSnapshot> previousSnapshots = snapshotPreviousValues(targetKeys);
        applySnapshots(updatedSnapshots);
        registerRollbackRestore(targetKeys, previousSnapshots);

        return updatedSnapshots;
    }

    private Map<FeatureFlagKey, FeatureFlagSnapshot> snapshotPreviousValues(Set<FeatureFlagKey> targetKeys) {
        Map<FeatureFlagKey, FeatureFlagSnapshot> previous = new HashMap<>();
        for (FeatureFlagKey key : targetKeys) {
            FeatureFlagSnapshot snapshot = cache.get(key);
            if (snapshot != null) {
                previous.put(key, snapshot);
            }
        }
        return previous;
    }

    private void applySnapshots(List<FeatureFlagSnapshot> snapshots) {
        snapshots.forEach(snapshot -> cache.put(snapshot.featureKey(), snapshot));
    }

    private void registerRollbackRestore(
            Set<FeatureFlagKey> targetKeys, Map<FeatureFlagKey, FeatureFlagSnapshot> previousSnapshots) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    return;
                }

                for (FeatureFlagKey targetKey : targetKeys) {
                    FeatureFlagSnapshot previous = previousSnapshots.get(targetKey);
                    if (previous == null) {
                        cache.remove(targetKey);
                    } else {
                        cache.put(targetKey, previous);
                    }
                }
            }
        });
    }
}
