package aegis.server.global.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS);

    @Value("${google.credentials.json}")
    private String credentialsJson;

    @Bean
    public Sheets sheets() throws GeneralSecurityException, IOException {
        // Base64로 인코딩된 JSON 문자열을 디코딩
        byte[] decodedBytes = Base64.getDecoder().decode(credentialsJson);

        // 디코딩된 바이트 배열로부터 GoogleCredentials 생성
        GoogleCredentials credential = GoogleCredentials.fromStream(
                new ByteArrayInputStream(decodedBytes)
        ).createScoped(SCOPES);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credential))
                .setApplicationName("aegis")
                .build();
    }
}
