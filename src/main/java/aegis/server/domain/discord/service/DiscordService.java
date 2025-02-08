package aegis.server.domain.discord.service;

import aegis.server.domain.discord.domain.DiscordVerification;
import aegis.server.domain.discord.dto.response.DiscordIdResponse;
import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.repository.DiscordVerificationRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiscordService {

    private final DiscordVerificationRepository discordVerificationRepository;
    private final MemberRepository memberRepository;

    public DiscordIdResponse getDiscordId(UserDetails userDetails) {
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return DiscordIdResponse.of(member.getDiscordId());
    }

    @Transactional
    public DiscordVerificationCodeResponse createVerificationCode(UserDetails userDetails) {
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        String code = generateUniqueCode();
        discordVerificationRepository.save(DiscordVerification.of(code, member.getId()));

        return DiscordVerificationCodeResponse.of(code);
    }

    // DiscordSlashCommandListener에서 사용
    @Transactional
    public void verifyAndUpdateDiscordId(String verificationCode, String discordId) {
        DiscordVerification discordVerification = discordVerificationRepository.findById(verificationCode)
                .orElseThrow(NoSuchElementException::new); // 메서드가 try-catch문 안에서 호출되므로 여기서 CustomException을 발생시키지 않는다

        Member member = memberRepository.findById(discordVerification.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateDiscordId(discordId);

        discordVerificationRepository.delete(discordVerification);

        log.info(
                "[DiscordService] 디스코드 연동 완료: memberId={}, discordId={}",
                member.getId(),
                member.getDiscordId()
        );
    }

    public String generateUniqueCode() {
        String code;
        int maxAttempts = 100;
        int attempts = 0;
        do {
            if (attempts++ >= maxAttempts) {
                throw new CustomException(ErrorCode.DISCORD_CANNOT_ISSUE_VERIFICATION_CODE);
            }
            code = generateRandomCode();
        } while (discordVerificationRepository.existsById(code));
        return code;
    }

    private String generateRandomCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }
}
