package aegis.server.domain.member.repository;

import aegis.server.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOidcId(String oidcId);
}
