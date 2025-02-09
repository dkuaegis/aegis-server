package aegis.server.domain.timetable.dto.internal;

public record LectureInfo(
        String name,
        String professor,
        String time,
        Integer credit,
        String place
) {
}
