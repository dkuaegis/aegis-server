package aegis.server.domain.discord.repository;

import aegis.server.domain.discord.domain.DiscordVerification;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DiscordVerificationRepository extends CrudRepository<DiscordVerification, String> {

    List<DiscordVerification> findAll();

    Optional<DiscordVerification> findByMemberId(Long memberId);
}
