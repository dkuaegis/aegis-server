package aegis.server.domain.discord.service.listener;

import aegis.server.domain.payment.domain.event.MissingDepositorNameEvent;
import aegis.server.domain.payment.domain.event.OverpaidEvent;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordEventListener {

    private final JDA jda;

    @Value("${discord.guild-id}")
    private String guildId;

    @Value("${discord.complete-role-id}")
    private String roleId;

    @Value("${discord.alarm-channel-id}")
    private String alarmChannelId;

    @EventListener
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        String discordId = event.payment().getMember().getDiscordId();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Discord 서버를 찾을 수 없습니다");
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            throw new IllegalStateException("역할을 찾을 수 없습니다");
        }

        guild.addRoleToMember(UserSnowflake.fromId(discordId), role).queue();

        log.info(
                "[DiscordEventListener][PaymentCompletedEvent] 디스코드 회원 역할 승급: paymentId={}, memberId={}, discordId={}",
                event.payment().getId(),
                event.payment().getMember().getId(),
                discordId
        );
    }

    @EventListener
    public void handleMissingDepositorNameEvent(MissingDepositorNameEvent event) {
        alarmChannel().sendMessage(
                String.format(
                        "[MISSING_DEPOSITOR_NAME]\nTX ID: %s 입금자명: %s",
                        event.transaction().getId(),
                        event.transaction().getDepositorName()
                )
        ).queue();
    }

    @EventListener
    public void handleOverpaidEvent(OverpaidEvent event) {
        alarmChannel().sendMessage(
                String.format(
                        "[OVERPAID]\nTX ID: %s 입금자명: %s",
                        event.transaction().getId(),
                        event.transaction().getDepositorName()
                )
        ).queue();
    }

    private TextChannel alarmChannel() {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Discord 서버를 찾을 수 없습니다");
        }

        return guild.getTextChannelById(alarmChannelId);
    }
}
