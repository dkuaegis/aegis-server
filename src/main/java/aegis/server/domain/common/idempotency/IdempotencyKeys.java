package aegis.server.domain.common.idempotency;

/**
 * 멱등키 생성 유틸리티 </br>
 * 현재는 포인트 지급과 관련된 멱등키만 생성 </br>
 * </br>
 * 규격: ipk:{version}:{service}:{action}:{resource-type}:{resource-id}:member:{member-id} </br>
 * - version: v1 </br>
 * - service: point </br>
 * - action: earn | spend </br>
 * - resource-type: study-session | study-session-instructor | activity </br>
 * - resource-id: 리소스의 고유 ID (스터디 세션 ID, 활동 ID 등) </br>
 * - member-id: 포인트를 받는 회원의 ID
 */
public final class IdempotencyKeys {

    private IdempotencyKeys() {}

    public static String forStudyAttendance(Long sessionId, Long participantMemberId) {
        requirePositive(sessionId, "sessionId");
        requirePositive(participantMemberId, "participantMemberId");
        return format("ipk:v1:point:earn:study-session:%d:member:%d", sessionId, participantMemberId);
    }

    public static String forStudyInstructor(Long sessionId, Long instructorId) {
        requirePositive(sessionId, "sessionId");
        requirePositive(instructorId, "instructorId");
        return format("ipk:v1:point:earn:study-session-instructor:%d:member:%d", sessionId, instructorId);
    }

    public static String forActivity(Long activityId, Long memberId) {
        requirePositive(activityId, "activityId");
        requirePositive(memberId, "memberId");
        return format("ipk:v1:point:earn:activity:%d:member:%d", activityId, memberId);
    }

    private static String format(String pattern, Object... args) {
        return String.format(pattern, args).toLowerCase();
    }

    private static void requirePositive(Long v, String name) {
        if (v == null || v <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
