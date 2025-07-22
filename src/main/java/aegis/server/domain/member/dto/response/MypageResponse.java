package aegis.server.domain.member.dto.response;

import java.math.BigDecimal;

import aegis.server.domain.member.domain.ProfileIcon;

public record MypageResponse(String name, ProfileIcon profileIcon, BigDecimal pointBalance) {

    public static MypageResponse of(String name, ProfileIcon profileIcon, BigDecimal pointBalance) {
        return new MypageResponse(name, profileIcon, pointBalance);
    }
}
