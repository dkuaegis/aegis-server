package aegis.server.domain.qrcode.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import aegis.server.domain.qrcode.domain.QRCode;

public interface QRCodeRepository extends CrudRepository<QRCode, UUID> {

    Optional<QRCode> findByMemberId(Long memberId);
}
