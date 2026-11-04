package aegis.server.domain.study.dto.response;

public record TodayAttendanceResponse(boolean attended) {

    public static TodayAttendanceResponse of(boolean attended) {
        return new TodayAttendanceResponse(attended);
    }
}
