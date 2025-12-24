package aegis.server.domain.discord.service;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.discord.domain.DiscordVerification;
import aegis.server.domain.discord.dto.response.DiscordIdResponse;
import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.repository.DiscordVerificationRepository;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import aegis.server.helper.RedisCleaner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DiscordServiceTest extends IntegrationTest {

    @Autowired
    DiscordService discordService;

    @Autowired
    DiscordVerificationRepository discordVerificationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RedisCleaner redisCleaner;

    @MockitoBean
    DiscordMembershipChecker discordMembershipChecker;

    @BeforeEach
    void setUp() {
        redisCleaner.clean();
    }

    private static final String DISCORD_ID = "1234567890";

    @Nested
    class 디스코드_ID_조회 {
        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            member.updateDiscordId(DISCORD_ID);
            memberRepository.save(member);
            UserDetails userDetails = createUserDetails(member);
            when(discordMembershipChecker.isMember(DISCORD_ID)).thenReturn(true);

            // when
            DiscordIdResponse response = discordService.getDiscordId(userDetails);

            // then
            assertEquals(DISCORD_ID, response.discordId());
        }

        @Test
        void 서버_미가입이면_null을_반환한다() {
            // given
            Member member = createMember();
            member.updateDiscordId(DISCORD_ID);
            memberRepository.save(member);
            UserDetails userDetails = createUserDetails(member);
            when(discordMembershipChecker.isMember(DISCORD_ID)).thenReturn(false);

            // when
            DiscordIdResponse response = discordService.getDiscordId(userDetails);

            // then
            assertNull(response.discordId());
        }

        @Test
        void 디스코드ID가_없어도_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            DiscordIdResponse response = discordService.getDiscordId(userDetails);

            // then
            assertNull(response.discordId());
        }

        @Test
        void 존재하지_않는_멤버이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            memberRepository.delete(member);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> discordService.getDiscordId(userDetails));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 인증코드_생성 {
        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            DiscordVerificationCodeResponse response = discordService.createVerificationCode(userDetails);

            // then
            // 반환값 검증
            assertNotNull(response.code());
            assertEquals(6, response.code().length());

            // DB 상태 검증
            DiscordVerification verification =
                    discordVerificationRepository.findById(response.code()).get();
            assertEquals(member.getId(), verification.getMemberId());
            assertEquals(response.code(), verification.getCode());
        }

        @Test
        void 기존_인증코드가_있으면_그대로_반환한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            DiscordVerificationCodeResponse firstResponse = discordService.createVerificationCode(userDetails);

            // when
            DiscordVerificationCodeResponse secondResponse = discordService.createVerificationCode(userDetails);

            // then
            assertEquals(firstResponse.code(), secondResponse.code());
            assertEquals(1, discordVerificationRepository.count());
        }

        @Test
        void 존재하지_않는_멤버이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            memberRepository.delete(member);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> discordService.createVerificationCode(userDetails));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 디스코드_연동 {
        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            DiscordVerificationCodeResponse codeResponse = discordService.createVerificationCode(userDetails);
            String code = codeResponse.code();

            // when
            discordService.verifyAndUpdateDiscordId(code, DISCORD_ID);

            // then
            // DB 상태 검증
            Member updatedMember = memberRepository.findById(member.getId()).get();
            assertEquals(DISCORD_ID, updatedMember.getDiscordId());
            assertEquals(0, discordVerificationRepository.count());
        }

        @Test
        void 존재하지_않는_인증코드이면_실패한다() {
            // when-then
            assertThrows(
                    NoSuchElementException.class,
                    () -> discordService.verifyAndUpdateDiscordId("WRONG_CODE", DISCORD_ID));
        }

        @Test
        void 만료된_인증코드이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            DiscordVerificationCodeResponse response = discordService.createVerificationCode(userDetails);
            discordVerificationRepository.deleteById(response.code());

            // when-then
            assertThrows(
                    NoSuchElementException.class,
                    () -> discordService.verifyAndUpdateDiscordId(response.code(), DISCORD_ID));

            // DB 상태 검증 - 멤버의 디스코드 ID는 변경되지 않아야 함
            Member member2 = memberRepository.findById(member.getId()).get();
            assertNull(member2.getDiscordId());
        }
    }
}
