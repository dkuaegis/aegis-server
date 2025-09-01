package aegis.server.domain.discord.service;

import aegis.server.domain.member.domain.Member;

public final class DiscordUtil {

    // 학번이 유효하다면 `입학연도(YY) 이름` 형태로,
    // 유효하지 않다면 `이름` 형태로 닉네임을 포맷팅합니다.
    public static String formatNickname(Member member) {
        return member.getEntranceYearYY().map(yy -> yy + " " + member.getName()).orElse(member.getName());
    }
}
