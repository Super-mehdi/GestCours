package ma.emi.backend.repository;

import jakarta.validation.constraints.NotBlank;
import ma.emi.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCode(@NotBlank(message = "Course's code is compulsory") String code);
}
