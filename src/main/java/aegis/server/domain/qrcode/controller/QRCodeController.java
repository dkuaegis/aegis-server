package aegis.server.domain.qrcode.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.qrcode.service.QRCodeService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "QRCode", description = "QR 코드 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/qrcode")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @Operation(
            summary = "QR 코드 발급",
            description = "사용자 QR 코드를 발급합니다. 기존 QR 코드가 있으면 삭제 후 새로 발급합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "QR 코드 발급 성공",
                        content = @Content(mediaType = "text/plain")),
                @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            })
    @PostMapping("/issue")
    public ResponseEntity<String> issueQRCode(@Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        String qrCode = qrCodeService.issueQRCode(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(qrCode);
    }
}
