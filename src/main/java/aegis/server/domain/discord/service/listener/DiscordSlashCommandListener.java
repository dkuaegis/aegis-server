package aegis.server.domain.discord.service.listener;

import aegis.server.domain.discord.service.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordSlashCommandListener extends ListenerAdapter {

    private final DiscordService discordService;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("join")) return;

        OptionMapping eventOption = event.getOption("code");
        if (eventOption == null) {
            event.reply("인증코드를 입력해주세요.").setEphemeral(true).queue();
            return;
        }
        String verificationCode = eventOption.getAsString();
        String discordId = event.getUser().getId();

        try {
            discordService.verifyAndUpdateDiscordId(verificationCode, discordId);
            event.reply("디스코드 연동이 완료되었습니다.")
                    .setEphemeral(true)
                    .queue();
        } catch (Exception e) {
            event.reply("인증코드가 올바르지 않거나 만료되었습니다.")
                    .setEphemeral(true)
                    .queue();
            log.warn(
                    "[DiscordSlashCommandListener] 디스코드 연동 실패: discordId={}, verificationCode={}",
                    discordId,
                    verificationCode
            );
        }
    }
}
