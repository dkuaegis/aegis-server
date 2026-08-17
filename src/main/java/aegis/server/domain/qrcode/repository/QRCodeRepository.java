package aegis.server.domain.qrcode.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.qrcode.domain.QRCode;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    Optional<QRCode> findByMemberId(Long memberId);

    Optional<QRCode> findByTokenAndExpiresAtAfter(UUID token, LocalDateTime now);
}
