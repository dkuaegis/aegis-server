package aegis.server.domain.study.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@RedisHash(value = "study_attendance_code", timeToLive = 300)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyAttendanceCode {

    @Id
    private String code;

    @Indexed
    private Long sessionId;

    private Long issuerMemberId;

    public static StudyAttendanceCode of(String code, Long sessionId, Long issuerMemberId) {
        return new StudyAttendanceCode(code, sessionId, issuerMemberId);
    }
}
