package aegis.server.domain.qrcode.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.qrcode.dto.response.QRCodeMemberResponse;
import aegis.server.domain.qrcode.service.QRCodeService;

@Tag(name = "Admin QRCode", description = "관리자용 QR 코드 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/qrcode")
public class AdminQRCodeController {

    private final QRCodeService qrCodeService;

    @Operation(
            summary = "QR로 회원 조회",
            description = "QR코드의 UUID로 Redis에서 조회하여 회원의 memberId, name, studentId를 반환합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "400", description = "UUID 포맷 오류", content = @Content),
                @ApiResponse(responseCode = "404", description = "QR 또는 회원을 찾을 수 없음", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/{uuid}")
    public ResponseEntity<QRCodeMemberResponse> getMemberByQrCodeUuid(@PathVariable String uuid) {
        QRCodeMemberResponse response = qrCodeService.findMemberByQrCodeUuid(uuid);
        return ResponseEntity.ok(response);
    }
}
