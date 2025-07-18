package aegis.server.domain.qrcode.service;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.qrcode.domain.QRCode;
import aegis.server.domain.qrcode.repository.QRCodeRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest extends IntegrationTest {

    @Autowired
    QRCodeService qrCodeService;

    @Autowired
    QRCodeRepository qrCodeRepository;

    @Nested
    class IssueQRCode {

        @Test
        void 새로운_QR코드_발급() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            String qrCode = qrCodeService.issueQRCode(userDetails);

            // then
            QRCode savedQRCode = qrCodeRepository.findByMemberId(member.getId()).get();
            assertEquals(member.getId(), savedQRCode.getMemberId());
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
        void 기존_QR코드가_있는_경우_삭제_후_새로_발급() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            qrCodeService.issueQRCode(userDetails);

            // when
            qrCodeService.issueQRCode(userDetails);

            // then
            long count = qrCodeRepository.count();
            assertEquals(1, count);
        }
    }
}
