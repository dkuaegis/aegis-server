package aegis.server.domain.discord.service.listener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.event.MismatchEvent;
import aegis.server.domain.payment.domain.event.NameConflictEvent;
import aegis.server.domain.pointshop.domain.event.PointShopDrawnEvent;
import aegis.server.domain.pointshop.dto.internal.PointShopDrawInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordEventListener {

    private static final String DIVIDER = "───────────────────────";

    private final JDA jda;
    private final MemberRepository memberRepository;

    @Value("${discord.guild-id}")
    private String guildId;

    @Value("${discord.alarm-channel-id}")
    private String alarmChannelId;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMismatchEvent(MismatchEvent event) {
        alarmChannel()
                .sendMessage(String.format(
                        "## [MISMATCH]\n**TX ID**: %s\n**입금자명**: %s\n**입금 금액**: %s\n%s",
                        event.transactionInfo().id(),
                        event.transactionInfo().depositorName(),
                        event.transactionInfo().amount(),
                        DIVIDER))
                .queue();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNameConflictEvent(NameConflictEvent event) {
        alarmChannel()
                .sendMessage(String.format(
                        "## [NAME_CONFLICT]\n동명이인 결제 충돌\n**TX ID**: %s\n**입금자명**: %s\n**입금 금액**: %s\n**해당 회원 ID**: %s\n%s",
                        event.transactionInfo().id(),
                        event.transactionInfo().depositorName(),
                        event.transactionInfo().amount(),
                        event.memberIds(),
                        DIVIDER))
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
                        "## [POINT_SHOP_DRAW]\n**상품**: %s\n**회원 ID**: %s\n**이름**: %s\n**학번**: %s\n**전화번호**: %s\n%s",
                        info.item(), info.memberId(), name, studentId, phoneNumber, DIVIDER))
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
