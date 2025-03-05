package aegis.server.domain.survey.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AcquisitionType {
    KAKAOTALK("카카오톡"),
    INSTAGRAM("인스타그램"),
    EVERYTIME("에브리타임"),
    FRIEND("지인"),
    CLUB_FAIR("동아리알림제"),
    ETC("기타");

    private final String value;
}
