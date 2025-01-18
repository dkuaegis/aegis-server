package aegis.server.domain.discord.repository;

import aegis.server.domain.discord.domain.DiscordVerification;
import org.springframework.data.repository.CrudRepository;

public interface DiscordVerificationRepository extends CrudRepository<DiscordVerification, String> {
}
