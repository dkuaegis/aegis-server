package aegis.server.domain.member.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOidcId(String oidcId);

    @Query("SELECT m FROM Member m WHERE m.role != :role AND m.id NOT IN :excludedIds")
    List<Member> findAllByRoleNotAndIdNotIn(@Param("role") Role role, @Param("excludedIds") List<Long> excludedIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :memberId")
    Optional<Member> findByIdWithLock(Long memberId);

    // 결제완료자 ID 목록이 비어 있는 경우를 위한 단순 조회
    List<Member> findAllByRoleNot(Role role);
}
