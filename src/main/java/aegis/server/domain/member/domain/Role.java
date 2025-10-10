package aegis.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    STAFF("ROLE_STAFF"),
    ADMIN("ROLE_ADMIN");

    private final String key;
}
