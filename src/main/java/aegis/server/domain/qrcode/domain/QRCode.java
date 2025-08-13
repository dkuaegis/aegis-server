package aegis.server.domain.qrcode.domain;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@RedisHash(value = "qr_code", timeToLive = 60)
@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class QRCode {

    @Id
    private UUID id;

    @Indexed
    private Long memberId;

    public static QRCode of(UUID id, Long memberId) {
        return new QRCode(id, memberId);
    }
}
