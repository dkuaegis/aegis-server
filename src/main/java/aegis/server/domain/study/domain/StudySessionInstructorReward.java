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
public class StudySessionInstructorReward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_session_instructor_reward_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id")
    private StudySession studySession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member instructor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_transaction_id")
    private PointTransaction pointTransaction;

    public static StudySessionInstructorReward create(
            StudySession studySession, Member instructor, PointTransaction pointTransaction) {
        return StudySessionInstructorReward.builder()
                .studySession(studySession)
                .instructor(instructor)
                .pointTransaction(pointTransaction)
                .build();
    }
}
