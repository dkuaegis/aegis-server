package aegis.server.domain.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.activity.domain.ActivityParticipation;

public interface ActivityParticipationRepository extends JpaRepository<ActivityParticipation, Long> {}
