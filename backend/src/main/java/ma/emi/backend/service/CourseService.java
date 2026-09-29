package ma.emi.backend.service;

import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.CourseRequestDto;
import ma.emi.backend.dto.response.CourseResponseDto;
import ma.emi.backend.entity.Course;
import ma.emi.backend.exception.CourseNotFoundException;
import ma.emi.backend.exception.DuplicateCourseException;
import ma.emi.backend.mapper.CourseMapper;
import ma.emi.backend.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Transactional
    public CourseResponseDto createCourse(CourseRequestDto request) {
        if (this.courseRepository.existsByCode(request.code())){
            throw new DuplicateCourseException("A course with the same code already exists.");
        }
        Course course = courseMapper.toEntity(request);
        Course savedCourse = this.courseRepository.save(course);
        return courseMapper.toDto(savedCourse);
    }

    public CourseResponseDto getCourseById(Long id) {
        Course course = this.courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException("No course with the corresponding id was found"));
        return courseMapper.toDto(course);
    }

    public List<CourseResponseDto> getAllCourses() {
        List<Course> courses = this.courseRepository.findAll();
        return courses.stream().map(this.courseMapper::toDto).toList();
    }

    @Transactional
    public CourseResponseDto updateCourse(CourseRequestDto request, Long id) {
        Course course = this.courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException("No course with the corresponding id was found"));
        course.setName(request.name());
        course.setDescription(request.description());
        course.setCapacity(request.capacity());
        return this.courseMapper.toDto(course);
    }

    @Transactional
    public CourseResponseDto deleteCourse(Long id) {
        Course course = this.courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException("No course with the corresponding id was found"));
        this.courseRepository.delete(course);
        return this.courseMapper.toDto(course);
    }


}
