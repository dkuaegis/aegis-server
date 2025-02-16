package aegis.server.global.config;

import aegis.server.domain.timetable.dto.external.EverytimeResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class TimetableConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders timetableHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(
                Map.of(
                        HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8",
                        HttpHeaders.CONNECTION, "keep-alive",
                        HttpHeaders.PRAGMA, "no-cache",
                        HttpHeaders.HOST, "api.everytime.kr",
                        HttpHeaders.ORIGIN, "https://everytime.kr",
                        HttpHeaders.REFERER, "https://everytime.kr",
                        HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
                )
        );

        return headers;
    }

    @Bean
    public JAXBContext jaxbContext() throws JAXBException {
        return JAXBContext.newInstance(EverytimeResponse.class);
    }

    @Bean
    public PasswordEncoder bcryptEncoder() {
        return new BCryptPasswordEncoder(4);
    }
}
