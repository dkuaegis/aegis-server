package aegis.server.domain.qrcode.service;

import java.util.Base64;

import aegis.server.domain.qrcode.dto.response.QRCodeMemberResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.qrcode.domain.QRCode;
import aegis.server.domain.qrcode.repository.QRCodeRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import aegis.server.helper.RedisCleaner;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest extends IntegrationTest {

    @Autowired
    QRCodeService qrCodeService;

    @Autowired
    QRCodeRepository qrCodeRepository;

    @Autowired
    RedisCleaner redisCleaner;

    @BeforeEach
    void setUp() {
        redisCleaner.clean();
    }

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

    @Nested
    class QR코드의_UUID로_회원조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            qrCodeService.issueQRCode(userDetails);

            // when
            QRCode savedQRCode = qrCodeRepository.findByMemberId(member.getId()).get();
            QRCodeMemberResponse response =
                    qrCodeService.findMemberByQrCodeUuid(savedQRCode.getId().toString());

            // then
            assertEquals(member.getId(), response.memberId());
            assertEquals(member.getName(), response.name());
            assertEquals(member.getStudentId(), response.studentId());
        }

        @Test
        void QR코드가_만료된_경우_실패한다() {
            // given
            String randomUuid = java.util.UUID.randomUUID().toString();

            // then
            assertThrows(aegis.server.global.exception.CustomException.class, () -> {
                // when
                qrCodeService.findMemberByQrCodeUuid(randomUuid);
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
