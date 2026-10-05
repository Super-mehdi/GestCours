package ma.emi.backend.repository;

import ma.emi.backend.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration,Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    java.util.List<Registration> findByStudentId(Long studentId);
    java.util.List<Registration> findByStatus(ma.emi.backend.entity.RegistrationStatus status);
    java.util.Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
