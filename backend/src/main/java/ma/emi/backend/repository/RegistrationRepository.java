package ma.emi.backend.repository;

import ma.emi.backend.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration,Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    java.util.List<Registration> findByStudentId(Long studentId);
}
