package aegis.server.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOidcId(String oidcId);

    @Query("SELECT m FROM Member m WHERE m.role != :role AND m.id NOT IN :excludedIds")
    List<Member> findAllByRoleNotAndIdNotIn(@Param("role") Role role, @Param("excludedIds") List<Long> excludedIds);
}
