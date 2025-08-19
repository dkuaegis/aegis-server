package aegis.server.domain.qrcode.dto.response;

public record QrCodeResponse(String qrCodeImage) {
    public static QrCodeResponse from(String qrCodeImage) {
        return new QrCodeResponse(qrCodeImage);
    }
}
