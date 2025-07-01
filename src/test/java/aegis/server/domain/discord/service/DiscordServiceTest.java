package aegis.server.domain.discord.service;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.repository.DiscordVerificationRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

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
    void 디스코드_연동_성공() {
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

    @Nested
    class 디스코드_연동_실패 {

        @Test
        void 잘못된_인증_코드() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            discordService.createVerificationCode(userDetails);

            // when-then
            assertThrows(NoSuchElementException.class,
                    () -> discordService.verifyAndUpdateDiscordId("WRONG_CODE", "1234"));
            assertEquals(1, discordVerificationRepository.count());
            assertNull(memberRepository.findById(member.getId()).get().getDiscordId());
        }

        @Test
        void 만료된_인증_코드() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            DiscordVerificationCodeResponse response = discordService.createVerificationCode(userDetails);

            // when
            discordVerificationRepository.deleteById(response.getCode());

            // then
            assertThrows(NoSuchElementException.class,
                    () -> discordService.verifyAndUpdateDiscordId(response.getCode(), "1234"));
            assertNull(memberRepository.findById(member.getId()).get().getDiscordId());
        }

        @Test
        void 이미_사용된_인증_코드() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            DiscordVerificationCodeResponse response = discordService.createVerificationCode(userDetails);
            discordService.verifyAndUpdateDiscordId(response.getCode(), "1234");

            // when-then
            assertThrows(NoSuchElementException.class,
                    () -> discordService.verifyAndUpdateDiscordId(response.getCode(), "1234"));
            assertEquals(0, discordVerificationRepository.count());
            assertEquals("1234", memberRepository.findById(member.getId()).get().getDiscordId());
        }
    }
}
