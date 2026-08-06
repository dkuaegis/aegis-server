package aegis.server.domain.qrcode.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        indexes = {@Index(name = "idx_qrcode_expires_at", columnList = "expires_at")},
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_qrcode_token", columnNames = "token"),
            @UniqueConstraint(name = "uk_qrcode_member", columnNames = "member_id")
        })
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qrcode_id")
    private Long id;

    @Column(nullable = false)
    private UUID token;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static QRCode create(UUID token, Long memberId, LocalDateTime expiresAt) {
        return QRCode.builder()
                .token(token)
                .memberId(memberId)
                .expiresAt(expiresAt)
                .build();
    }

    public void reissue(UUID token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
