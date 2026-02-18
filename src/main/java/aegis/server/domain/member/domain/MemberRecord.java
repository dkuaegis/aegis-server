package aegis.server.domain.member.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;

@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_member_record_member_year_semester",
                    columnNames = {"member_id", "year_semester"})
        },
        indexes = {
            @Index(name = "idx_member_record_year_semester_id", columnList = "year_semester, member_record_id"),
            @Index(name = "idx_member_record_member_id_id", columnList = "member_id, member_record_id")
        })
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "year_semester", nullable = false)
    private YearSemester yearSemester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRecordSource recordSource;

    private String snapshotStudentId;

    @Column(nullable = false)
    private String snapshotName;

    @Column(nullable = false)
    private String snapshotEmail;

    private String snapshotPhoneNumber;

    @Enumerated(EnumType.STRING)
    private Department snapshotDepartment;

    @Enumerated(EnumType.STRING)
    private Grade snapshotGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role snapshotRole;

    private Long paymentId;

    private LocalDateTime paymentCompletedAt;

    public static MemberRecord create(
            Member member,
            YearSemester yearSemester,
            MemberRecordSource recordSource,
            Long paymentId,
            LocalDateTime paymentCompletedAt) {
        return MemberRecord.builder()
                .member(member)
                .yearSemester(yearSemester)
                .recordSource(recordSource)
                .snapshotStudentId(member.getStudentId())
                .snapshotName(member.getName())
                .snapshotEmail(member.getEmail())
                .snapshotPhoneNumber(member.getPhoneNumber())
                .snapshotDepartment(member.getDepartment())
                .snapshotGrade(member.getGrade())
                .snapshotRole(member.getRole())
                .paymentId(paymentId)
                .paymentCompletedAt(paymentCompletedAt)
                .build();
    }
}
