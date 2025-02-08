package aegis.server.domain.discord.service;

import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.repository.DiscordVerificationRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiscordServiceTest extends IntegrationTest {

    @Autowired
    DiscordService discordService;

    @Autowired
    DiscordVerificationRepository discordVerificationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 인증코드_생성() {
        // given
        Member member = createMember();
        UserDetails userDetails = createUserDetails(member);

        // when
        DiscordVerificationCodeResponse response = discordService.createVerificationCode(userDetails);

        // then
        assertEquals(response.getCode(), discordVerificationRepository.findAll().getFirst().getCode());
    }

    @Test
    void 디스코드_ID_인증() {
        // given
        Member member = createMember();
        UserDetails userDetails = createUserDetails(member);
        DiscordVerificationCodeResponse response = discordService.createVerificationCode(userDetails);
        String code = response.getCode();
        String discordId = "1234";

        // when
        discordService.verifyAndUpdateDiscordId(code, discordId);

        // then
        assertEquals(0, discordVerificationRepository.count());
        assertEquals(discordId, memberRepository.findById(member.getId()).get().getDiscordId());
    }
}
