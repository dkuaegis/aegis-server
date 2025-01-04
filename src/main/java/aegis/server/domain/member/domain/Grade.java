package aegis.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Grade {

    ONE("1학년"),
    TWO("2학년"),
    THREE("3학년"),
    FOUR("4학년"),
    FIVE("5학년"),
    SIX("6학년");

    private final String gradeName;

}
