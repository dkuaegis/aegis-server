package aegis.server.domain.member.domain;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import jakarta.persistence.*;
import lombok.*;

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
            Semester semester
    ) {
        this.studentId = studentId;
        this.department = department;
        this.academicStatus = academicStatus;
        this.grade = grade;
        this.semester = semester;
    }
}
