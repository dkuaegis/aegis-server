package aegis.server.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.point.domain.PointAccount;

public interface PointAccountRepository extends JpaRepository<PointAccount, Long> {}
