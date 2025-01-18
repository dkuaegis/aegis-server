package aegis.server.global.config;

import aegis.server.domain.discord.service.listener.DiscordSlashCommandListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DiscordConfig {

    @Value("${discord.token}")
    private String token;

    private final DiscordSlashCommandListener discordSlashCommandListener;

    @Bean
    public JDA jda() {
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(discordSlashCommandListener)
                .build();

        jda.upsertCommand("join", "디스코드 연동 인증코드를 입력합니다.")
                .addOption(OptionType.STRING, "code", "인증코드", true)
                .queue();

        return jda;
    }
}
