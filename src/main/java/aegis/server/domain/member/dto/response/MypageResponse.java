package aegis.server.domain.member.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.member.domain.ProfileIcon;

public record MypageResponse(
        String name, @Schema(nullable = true) ProfileIcon profileIcon, BigDecimal pointBalance) {

    public static MypageResponse of(String name, ProfileIcon profileIcon, BigDecimal pointBalance) {
        return new MypageResponse(name, profileIcon, pointBalance);
    }
}
