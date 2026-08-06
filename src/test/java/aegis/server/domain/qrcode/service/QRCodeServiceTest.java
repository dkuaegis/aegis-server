package aegis.server.domain.qrcode.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.qrcode.domain.QRCode;
import aegis.server.domain.qrcode.dto.response.QRCodeMemberResponse;
import aegis.server.domain.qrcode.repository.QRCodeRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest extends IntegrationTest {

    @Autowired
    QRCodeService qrCodeService;

    @Autowired
    QRCodeRepository qrCodeRepository;

    @Autowired
    Clock clock;

    @Nested
    class QR코드_발급 {

        @Test
        void 새로운_QR코드_발급() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            String qrCode = qrCodeService.issueQRCode(userDetails);

            // then
            QRCode savedQRCode = qrCodeRepository.findByMemberId(member.getId()).orElseThrow();
            assertEquals(member.getId(), savedQRCode.getMemberId());
            assertTrue(savedQRCode.getExpiresAt().isAfter(LocalDateTime.now(clock)));
            assertFalse(qrCode.isEmpty());

            // base64 인코딩 검증
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(qrCode);
                assertTrue(decodedBytes.length > 0);
            } catch (IllegalArgumentException e) {
                throw new AssertionError("QR코드 이미지가 올바른 base64 형식이 아닙니다.");
            }
        }

        @Test
        void 기존_QR코드가_있는_경우_새로_발급() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            qrCodeService.issueQRCode(userDetails);
            UUID previousToken = qrCodeRepository
                    .findByMemberId(member.getId())
                    .orElseThrow()
                    .getToken();

            // when
            qrCodeService.issueQRCode(userDetails);

            // then
            QRCode reissuedQRCode =
                    qrCodeRepository.findByMemberId(member.getId()).orElseThrow();
            assertNotEquals(previousToken, reissuedQRCode.getToken());
            assertEquals(1, qrCodeRepository.count());
            assertThrows(
                    aegis.server.global.exception.CustomException.class,
                    () -> qrCodeService.findMemberByQrCodeUuid(previousToken.toString()));
        }
    }

    @Nested
    class QR코드의_UUID로_회원조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            qrCodeService.issueQRCode(userDetails);

            // when
            QRCode savedQRCode = qrCodeRepository.findByMemberId(member.getId()).orElseThrow();
            QRCodeMemberResponse response =
                    qrCodeService.findMemberByQrCodeUuid(savedQRCode.getToken().toString());

            // then
            assertEquals(member.getId(), response.memberId());
            assertEquals(member.getName(), response.name());
            assertEquals(member.getStudentId(), response.studentId());
        }

        @Test
        void QR코드가_만료된_경우_실패한다() {
            // given
            Member member = createMember();
            UUID expiredToken = UUID.randomUUID();
            QRCode expiredQRCode = QRCode.create(
                    expiredToken, member.getId(), LocalDateTime.now(clock).minusSeconds(1));
            qrCodeRepository.save(expiredQRCode);

            // then
            assertThrows(aegis.server.global.exception.CustomException.class, () -> {
                // when
                qrCodeService.findMemberByQrCodeUuid(expiredToken.toString());
            });
        }

        @Test
        void 잘못된_UUID인_경우_실패한다() {
            assertThrows(aegis.server.global.exception.CustomException.class, () -> {
                qrCodeService.findMemberByQrCodeUuid("not-a-uuid");
            });
        }
    }
}
