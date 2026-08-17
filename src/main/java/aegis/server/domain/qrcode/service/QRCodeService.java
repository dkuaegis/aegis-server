package aegis.server.domain.qrcode.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.qrcode.domain.QRCode;
import aegis.server.domain.qrcode.dto.response.QRCodeMemberResponse;
import aegis.server.domain.qrcode.repository.QRCodeRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QRCodeService {

    private static final long QR_CODE_VALIDITY_SECONDS = 60;

    private final QRCodeRepository qrCodeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional
    public String issueQRCode(UserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        memberRepository.findByIdWithLock(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UUID token = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusSeconds(QR_CODE_VALIDITY_SECONDS);
        QRCode qrCode = qrCodeRepository
                .findByMemberId(memberId)
                .map(existingQrCode -> {
                    existingQrCode.reissue(token, expiresAt);
                    return existingQrCode;
                })
                .orElseGet(() -> QRCode.create(token, memberId, expiresAt));
        qrCodeRepository.save(qrCode);

        return generateQrCodeImage(token.toString());
    }

    private String generateQrCodeImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new CustomException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }
    }

    public QRCodeMemberResponse findMemberByQrCodeUuid(String uuid) {
        UUID token;
        try {
            token = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        QRCode qrCode = qrCodeRepository
                .findByTokenAndExpiresAtAfter(token, LocalDateTime.now(clock))
                .orElseThrow(() -> new CustomException(ErrorCode.QR_CODE_NOT_FOUND));

        Long memberId = qrCode.getMemberId();
        Member member =
                memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return QRCodeMemberResponse.from(member);
    }
}
