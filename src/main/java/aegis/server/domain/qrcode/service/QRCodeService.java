package aegis.server.domain.qrcode.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private final QRCodeRepository qrCodeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String issueQRCode(UserDetails userDetails) {
        Long memberId = userDetails.getMemberId();

        // Redis에서 기존 QR코드 명시적으로 조회 후 삭제
        qrCodeRepository.findByMemberId(memberId).ifPresent(qrCodeRepository::delete);

        UUID qrCodeId = UUID.randomUUID();
        QRCode qrCode = QRCode.of(qrCodeId, memberId);
        qrCodeRepository.save(qrCode);

        return generateQrCodeImage(qrCodeId.toString());
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
        UUID id;
        try {
            id = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        QRCode qrCode =
                qrCodeRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.QR_CODE_NOT_FOUND));

        Long memberId = qrCode.getMemberId();
        Member member =
                memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return QRCodeMemberResponse.from(member);
    }
}
