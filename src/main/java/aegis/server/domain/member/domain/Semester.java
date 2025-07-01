package aegis.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Semester {
    FIRST("1학기"),
    SECOND("2학기");

    private final String value;
}
