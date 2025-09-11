package aegis.server.domain.study.domain;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"study_id", "session_date"}))
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    public static StudySession create(Study study, LocalDate sessionDate) {
        return StudySession.builder().study(study).sessionDate(sessionDate).build();
    }
}
