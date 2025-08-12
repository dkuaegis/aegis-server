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

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.event.MismatchEvent;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
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
