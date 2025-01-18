package aegis.server.domain.discord.service;

import aegis.server.domain.discord.domain.DiscordVerification;
import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.repository.DiscordVerificationRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiscordService {

    private final DiscordVerificationRepository discordVerificationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public DiscordVerificationCodeResponse createVerificationCode(SessionUser sessionUser) {
        Member member = memberRepository.findById(sessionUser.getId()).orElseThrow();
        String code = generateUniqueCode();
        discordVerificationRepository.save(DiscordVerification.of(code, member.getId()));

        return DiscordVerificationCodeResponse.of(code);
    }

    @Transactional
    public void verifyAndUpdateDiscordId(String verificationCode, String discordId) {
        DiscordVerification discordVerification = discordVerificationRepository.findById(verificationCode).orElseThrow();

        Member member = memberRepository.findById(discordVerification.getMemberId()).orElseThrow();

        member.updateDiscordId(discordId);

        discordVerificationRepository.delete(discordVerification);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (discordVerificationRepository.existsById(code));
        return code;
    }

    private String generateRandomCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}
