package aegis.server.domain.study.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointTransaction;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"study_session_id", "member_id"}))
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyAttendanceReward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_attendance_reward_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id")
    private StudySession studySession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member participant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_transaction_id")
    private PointTransaction pointTransaction;

    public static StudyAttendanceReward create(
            StudySession studySession, Member participant, PointTransaction pointTransaction) {
        return StudyAttendanceReward.builder()
                .studySession(studySession)
                .participant(participant)
                .pointTransaction(pointTransaction)
                .build();
    }
}
