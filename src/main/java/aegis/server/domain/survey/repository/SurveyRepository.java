package aegis.server.domain.survey.repository;

import aegis.server.domain.member.domain.Student;
import aegis.server.domain.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByStudent(Student student);

    List<Survey> findByStudentIn(List<Student> students);
}
