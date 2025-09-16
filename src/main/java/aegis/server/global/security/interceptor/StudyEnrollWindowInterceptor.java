package aegis.server.global.security.interceptor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyEnrollWindowInterceptor implements HandlerInterceptor {

    private final Clock clock;

    @Value("${study.enroll-window.open-at:}")
    private String openAtProp;

    @Value("${study.enroll-window.close-at:}")
    private String closeAtProp;

    private static final List<DateTimeFormatter> SUPPORTED_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME, // 2025-09-20T12:00[:ss]
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

    private volatile boolean enabled = false;
    private LocalDateTime openAt;
    private LocalDateTime closeAt;

    @PostConstruct
    void init() {
        if (isBlank(openAtProp) || isBlank(closeAtProp)) {
            log.warn("study.enroll-window.* 설정이 비어 있습니다. 인터셉터를 비활성화합니다.");
            this.enabled = false;
            return;
        }

        LocalDateTime parsedOpen = parseLocalDateTime(openAtProp);
        LocalDateTime parsedClose = parseLocalDateTime(closeAtProp);

        if (parsedOpen == null || parsedClose == null) {
            log.warn(
                    "study.enroll-window.* 설정 형식이 올바르지 않습니다. 인터셉터를 비활성화합니다. open='{}', close='{}'",
                    openAtProp,
                    closeAtProp);
            this.enabled = false;
            return;
        }

        if (!parsedClose.isAfter(parsedOpen)) {
            log.warn(
                    "study.enroll-window.close-at 이 open-at 보다 이후여야 합니다. 인터셉터를 비활성화합니다. open='{}', close='{}'",
                    openAtProp,
                    closeAtProp);
            this.enabled = false;
            return;
        }

        this.openAt = parsedOpen;
        this.closeAt = parsedClose;
        this.enabled = true;

        ZoneId zone = clock.getZone();
        log.info("스터디 참여 허용 시간 활성화: {} ~ {} (zone={})", openAt, closeAt, zone);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // POST 메소드만 검사
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 설정 누락/오류 시 무소음 통과
        if (!enabled) {
            return true;
        }

        ZoneId zone = clock.getZone();
        ZonedDateTime now = ZonedDateTime.now(clock);
        ZonedDateTime openZdt = openAt.atZone(zone);
        ZonedDateTime closeZdt = closeAt.atZone(zone);

        // 허용 구간: [openAt, closeAt) — openAt 이상, closeAt 미만만 허용
        boolean allowed = !now.isBefore(openZdt) && now.isBefore(closeZdt);

        if (!allowed) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static LocalDateTime parseLocalDateTime(String value) {
        for (DateTimeFormatter f : SUPPORTED_FORMATS) {
            try {
                return LocalDateTime.parse(value, f);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
