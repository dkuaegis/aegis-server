package aegis.server.domain.discord.service;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.discord.dto.response.DiscordDemoteResponse;
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

    public DiscordDemoteResponse demoteDiscordRolesForCurrentSemester() {
        // 1) 현재 학기 결제 완료 회원 ID 목록
        List<Long> paidMemberIds =
                paymentRepository
                        .findAllByStatusAndYearSemester(PaymentStatus.COMPLETED, CURRENT_YEAR_SEMESTER)
                        .stream()
                        .map(p -> p.getMember().getId())
                        .toList();

        // 2) ADMIN 제외 + 미납 회원 조회
        List<Member> unpaidMembers = memberRepository.findAllByRoleNotAndIdNotIn(ADMIN, paidMemberIds);

        // 3) 길드/역할 조회 및 유효성 검사
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new CustomException(ErrorCode.DISCORD_GUILD_NOT_FOUND);
        }
        Role role = guild.getRoleById(completeRoleId);
        if (role == null) {
            throw new CustomException(ErrorCode.DISCORD_ROLE_NOT_FOUND);
        }

        // 4) 역할 제거 실행 (비동기 처리)
        unpaidMembers.forEach(member -> {
            String discordId = member.getDiscordId();
            guild.removeRoleFromMember(UserSnowflake.fromId(discordId), role)
                    .queue(
                            v -> {},
                            exception -> log.warn(
                                    "[AdminDiscordService] 역할 제거 실패: memberId={} discordId={} reason={}",
                                    member.getId(),
                                    discordId,
                                    exception.toString()));
        });

        // 5) 강등 대상 학번 반환 (회원 강등 API와 응답 형태 일관)
        List<String> demotedStudentIds =
                unpaidMembers.stream().map(Member::getStudentId).toList();
        return DiscordDemoteResponse.of(demotedStudentIds);
    }
}
