package aegis.server.domain.discord.service;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

import static aegis.server.domain.member.domain.Role.ADMIN;
import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminDiscordService {

    private final JDA jda;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    @Value("${discord.guild-id}")
    private String guildId;

    @Value("${discord.complete-role-id}")
    private String completeRoleId;

    public void demoteDiscordRolesForCurrentSemester() {
        // 1) 현재 학기 결제 완료 회원 ID 목록
        List<Long> paidMemberIds =
                paymentRepository
                        .findAllByStatusAndYearSemester(PaymentStatus.COMPLETED, CURRENT_YEAR_SEMESTER)
                        .stream()
                        .map(p -> p.getMember().getId())
                        .toList();

        // 2) ADMIN 제외 + 미납 회원 조회
        List<Member> unpaidMembers = paidMemberIds.isEmpty()
                ? memberRepository.findAllByRoleNot(ADMIN) // 결제 완료자가 없으면 ADMIN 제외 전체
                : memberRepository.findAllByRoleNotAndIdNotIn(ADMIN, paidMemberIds);

        // 3) 길드/역할 조회 및 유효성 검사
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new CustomException(ErrorCode.DISCORD_GUILD_NOT_FOUND);
        }
        Role role = guild.getRoleById(completeRoleId);
        if (role == null) {
            throw new CustomException(ErrorCode.DISCORD_ROLE_NOT_FOUND);
        }

        // 4) 역할 제거 실행
        unpaidMembers.forEach(member -> {
            String discordId = member.getDiscordId();
            if (discordId == null || discordId.isBlank()) {
                log.info(
                        "[AdminDiscordService] 디스코드 ID가 없음으로 건너뜀: memberId={} studentId={}",
                        member.getId(),
                        member.getStudentId());
                return;
            }

            try {
                guild.removeRoleFromMember(UserSnowflake.fromId(discordId), role)
                        .queue( // 비동기 처리로 별도의 스레드에서 실행됨
                                v -> log.info(
                                        "[AdminDiscordService] 역할 제거 성공: memberId={} discordId={}",
                                        member.getId(),
                                        discordId),
                                exception -> {
                                    if (exception instanceof ErrorResponseException ere
                                            && ((ere.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER)
                                                    || (ere.getErrorResponse() == ErrorResponse.UNKNOWN_USER))) {
                                        // 이미 길드를 떠난 사용자 -> 정상 스킵 처리
                                        log.info(
                                                "[AdminDiscordService] 디스코드 서버 미가입(탈퇴)임으로 건너뜀: memberId={} discordId={} (UNKNOWN_MEMBER)",
                                                member.getId(),
                                                discordId);
                                    } else {
                                        log.warn(
                                                "[AdminDiscordService] 역할 제거 실패: memberId={} discordId={} reason={}",
                                                member.getId(),
                                                discordId,
                                                exception.toString());
                                    }
                                });
            } catch (IllegalArgumentException e) {
                // UserSnowflake.fromId에 유효하지 않은 값 전달 시 건너뜀
                log.info(
                        "[AdminDiscordService] 유효하지 않은 디스코드 ID임으로 건너뜀: memberId={} discordId={} reason={}",
                        member.getId(),
                        discordId,
                        e.getMessage());
            }
        });
    }
}
