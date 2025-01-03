package aegis.server.domain.survey.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InterestField {
    // 보안 분야
    SECURITY_WEBHACKING("웹해킹"),
    SECURITY_SYSTEMHACKING("시스템해킹"),
    SECURITY_REVERSING("리버싱"),
    SECURITY_FORENSIC("포렌식"),
    SECURITY_MALWARE("악성코드분석"),
    SECURITY_CRYPTOGRAPHY("암호학"),
    SECURITY_ETC("보안 기타"),

    // 웹 분야
    WEB_FRONTEND("프론트엔드"),
    WEB_BACKEND("백엔드"),
    WEB_ETC("웹 기타"),

    // 게임 분야
    GAME_CLIENT("클라이언트"),
    GAME_SERVER("서버"),
    GAME_ETC("게임 기타"),

    // 기타 분야
    APP("앱"),
    DEVOPS("DevOps"),
    AI("인공지능"),
    NOTSURE("아직 잘 모르겠어요"),
    ETC("기타");

    private final String description;

}


