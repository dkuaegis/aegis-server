package aegis.server.domain.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.domain.ActivityParticipation;
import aegis.server.domain.member.domain.Member;

public interface ActivityParticipationRepository extends JpaRepository<ActivityParticipation, Long> {

    boolean existsByActivityAndMember(Activity activity, Member member);
}
