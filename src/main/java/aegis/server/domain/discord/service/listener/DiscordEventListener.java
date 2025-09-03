package aegis.server.domain.discord.service.listener;

import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.discord.service.DiscordUtil;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.event.MismatchEvent;
import aegis.server.domain.payment.domain.event.NameConflictEvent;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.pointshop.domain.event.PointShopDrawnEvent;
import aegis.server.domain.pointshop.dto.internal.PointShopDrawInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordEventListener {

    private final JDA jda;
    private final MemberRepository memberRepository;

    @Value("${discord.guild-id}")
    private String guildId;

    @Value("${discord.complete-role-id}")
    private String roleId;

    @Value("${discord.alarm-channel-id}")
    private String alarmChannelId;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        Optional<Member> member = memberRepository.findById(event.paymentInfo().memberId());

        String discordId;
        if (member.isPresent()) {
            discordId = member.get().getDiscordId();
        } else {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new CustomException(ErrorCode.DISCORD_GUILD_NOT_FOUND);
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            throw new CustomException(ErrorCode.DISCORD_ROLE_NOT_FOUND);
        }

        guild.addRoleToMember(UserSnowflake.fromId(discordId), role).queue();

        String nickname = DiscordUtil.formatNickname(member.get());
        guild.retrieveMemberById(discordId)
                .queue(
                        m -> m.modifyNickname(nickname)
                                .queue(
                                        v -> log.info(
                                                "[DiscordEventListener][PaymentCompletedEvent] 닉네임 변경 성공: memberId={}, discordId={}, nickname={}",
                                                member.get().getId(),
                                                discordId,
                                                nickname),
                                        e -> log.warn(
                                                "[DiscordEventListener][PaymentCompletedEvent] 닉네임 변경 실패: memberId={}, discordId={}, nickname={}, reason={}",
                                                member.get().getId(),
                                                discordId,
                                                nickname,
                                                e.toString())),
                        e -> log.warn(
                                "[DiscordEventListener][PaymentCompletedEvent] 길드 멤버 조회 실패: memberId={}, discordId={}, reason={}",
                                member.get().getId(),
                                discordId,
                                e.toString()));

        log.info(
                "[DiscordEventListener][PaymentCompletedEvent] 디스코드 회원 역할 승급: paymentId={}, memberId={}, discordId={}",
                event.paymentInfo().id(),
                event.paymentInfo().memberId(),
                discordId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMismatchEvent(MismatchEvent event) {
        alarmChannel()
                .sendMessage(String.format(
                        "[MISMATCH]\nTX ID: %s 입금자명: %s 입금 금액: %s",
                        event.transactionInfo().id(),
                        event.transactionInfo().depositorName(),
                        event.transactionInfo().amount()))
                .queue();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNameConflictEvent(NameConflictEvent event) {
        alarmChannel()
                .sendMessage(String.format(
                        "[NAME_CONFLICT]\n동명이인 결제 충돌\nTX ID: %s\n입금자명: %s\n입금 금액: %s\n해당 회원 ID: %s",
                        event.transactionInfo().id(),
                        event.transactionInfo().depositorName(),
                        event.transactionInfo().amount(),
                        event.memberIds()))
                .queue();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointShopDrawnEvent(PointShopDrawnEvent event) {
        PointShopDrawInfo info = event.pointShopDrawInfo();

        Member member = memberRepository
                .findById(info.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String name = member.getName();
        String studentId = member.getStudentId() != null ? member.getStudentId() : "N/A";
        String phoneNumber = member.getPhoneNumber() != null ? member.getPhoneNumber() : "N/A";

        alarmChannel()
                .sendMessage(String.format(
                        "[POINT_SHOP_DRAW]\n상품: %s\n회원 ID: %s\n이름: %s\n학번: %s\n전화번호: %s",
                        info.item(), info.memberId(), name, studentId, phoneNumber))
                .queue();
    }

    private TextChannel alarmChannel() {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new CustomException(ErrorCode.DISCORD_GUILD_NOT_FOUND);
        }

        TextChannel textChannel = guild.getTextChannelById(alarmChannelId);
        if (textChannel == null) {
            throw new CustomException(ErrorCode.DISCORD_CHANNEL_NOT_FOUND);
        }

        return textChannel;
    }
}
