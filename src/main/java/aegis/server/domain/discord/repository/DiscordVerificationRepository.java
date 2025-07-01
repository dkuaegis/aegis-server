package aegis.server.domain.discord.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import aegis.server.domain.discord.domain.DiscordVerification;

public interface DiscordVerificationRepository extends CrudRepository<DiscordVerification, String> {

    List<DiscordVerification> findAll();

    Optional<DiscordVerification> findByMemberId(Long memberId);
}
