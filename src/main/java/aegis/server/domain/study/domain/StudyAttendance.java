package aegis.server.domain.study.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;

@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_study_attendance_session_member",
                    columnNames = {"study_session_id", "member_id"})
        })
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyAttendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_attendance_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "study_session_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_study_attendance_study_session"))
    private StudySession studySession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_study_attendance_member"))
    private Member member;

    public static StudyAttendance create(StudySession studySession, Member member) {
        return StudyAttendance.builder()
                .studySession(studySession)
                .member(member)
                .build();
    }
}
