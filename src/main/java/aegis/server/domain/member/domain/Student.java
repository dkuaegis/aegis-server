package aegis.server.domain.member.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "year_semester"})})
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_table_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    private String studentId;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private AcademicStatus academicStatus;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    // 신규 등록인지 확인하는 필드
    private Boolean fresh;

    public static Student from(Member member) {
        return Student.builder()
                .member(member)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .build();
    }

    public void updateStudent(
            String studentId,
            Department department,
            AcademicStatus academicStatus,
            Grade grade,
            Semester semester,
            Boolean fresh
    ) {
        this.studentId = studentId;
        this.department = department;
        this.academicStatus = academicStatus;
        this.grade = grade;
        this.semester = semester;
        this.fresh = fresh;
    }
}
