package aegis.server.domain.pointshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.pointshop.domain.PointShopDrawHistory;

public interface PointShopDrawHistoryRepository extends JpaRepository<PointShopDrawHistory, Long> {

    @Query("SELECT h FROM PointShopDrawHistory h WHERE h.member.id = :memberId")
    List<PointShopDrawHistory> findAllByMemberId(Long memberId);

    @Query("SELECT h FROM PointShopDrawHistory h WHERE h.member.id = :memberId ORDER BY h.id DESC")
    List<PointShopDrawHistory> findAllByMemberIdOrderByIdDesc(Long memberId);
}
