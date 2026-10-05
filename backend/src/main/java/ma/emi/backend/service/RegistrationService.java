package ma.emi.backend.service;

import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.RegistrationRequestDto;
import ma.emi.backend.dto.response.RegistrationResponseDto;
import ma.emi.backend.entity.Course;
import ma.emi.backend.entity.Registration;
import ma.emi.backend.entity.RegistrationStatus;
import ma.emi.backend.entity.Student;
import ma.emi.backend.exception.CourseNotFoundException;
import ma.emi.backend.exception.DuplicateRegistrationException;
import ma.emi.backend.exception.RegistrationNotFoundException;
import ma.emi.backend.exception.StudentNotFoundException;
import ma.emi.backend.mapper.RegistrationMapper;
import ma.emi.backend.repository.CourseRepository;
import ma.emi.backend.repository.RegistrationRepository;
import ma.emi.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RegistrationMapper registrationMapper;

    @Transactional
    public RegistrationResponseDto createRegistration(RegistrationRequestDto request) {
        Student student = this.studentRepository.findById(request.studentId()).orElseThrow(() -> new StudentNotFoundException("No student with the corresponding id was found"));
        Course course = this.courseRepository.findById(request.courseId()).orElseThrow(() -> new CourseNotFoundException("No course with the corresponding id was found"));
        if (this.registrationRepository.existsByStudentIdAndCourseId(request.studentId(),request.courseId())) {
            throw new DuplicateRegistrationException("This student is already registered to this course !");
        }
        Registration registration = this.registrationMapper.toEntity(request,course,student);
        Registration savedRegistration= this.registrationRepository.save(registration);
        return this.registrationMapper.toDto(savedRegistration);
    }

    public RegistrationResponseDto getRegistration(Long id) {
        return this.registrationMapper.toDto(this.registrationRepository.findById(id).orElseThrow(() -> new RegistrationNotFoundException("No registration with the corresponding id was found !")));
    }

    public List<RegistrationResponseDto> getAllRegistrations() {
        List<Registration> registrations = this.registrationRepository.findAll();
        return registrations.stream().map(this.registrationMapper::toDto).toList();
    }

    public List<RegistrationResponseDto> getRegistrationsByStudentId(Long studentId) {
        return this.registrationRepository.findByStudentId(studentId)
                .stream()
                .map(this.registrationMapper::toDto)
                .toList();
    }

    @Transactional
    public RegistrationResponseDto deleteRegistration(Long id) {
        Registration registration = this.registrationRepository.findById(id).orElseThrow(() -> new RegistrationNotFoundException("No registration with the corresponding id was found !"));
        this.registrationRepository.delete(registration);
        return this.registrationMapper.toDto(registration);
    }

    @Transactional
    public RegistrationResponseDto requestCourseEnrollment(Long studentId, Long courseId) {
        Student student = this.studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("No student with the corresponding id was found"));
        Course course = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("No course with the corresponding id was found"));

        if (this.registrationRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateRegistrationException("A registration or request for this course already exists!");
        }

        Registration registration = Registration.builder()
                .student(student)
                .course(course)
                .status(RegistrationStatus.PENDING)
                .build();

        Registration saved = this.registrationRepository.save(registration);
        return this.registrationMapper.toDto(saved);
    }

    public List<ma.emi.backend.dto.response.RegistrationDetailDto> getPendingRegistrations() {
        return this.registrationRepository.findByStatus(RegistrationStatus.PENDING)
                .stream()
                .map(r -> new ma.emi.backend.dto.response.RegistrationDetailDto(
                        r.getId(),
                        r.getStudent().getId(),
                        r.getStudent().getFirstName() + " " + r.getStudent().getLastName(),
                        r.getStudent().getEmail(),
                        r.getCourse().getId(),
                        r.getCourse().getCode(),
                        r.getCourse().getName(),
                        r.getStatus()
                ))
                .toList();
    }

    @Transactional
    public RegistrationResponseDto approveRegistration(Long id) {
        Registration registration = this.registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("No registration with id " + id + " found!"));
        registration.setStatus(RegistrationStatus.ACTIVE);
        return this.registrationMapper.toDto(registration);
    }

    @Transactional
    public RegistrationResponseDto rejectRegistration(Long id) {
        Registration registration = this.registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("No registration with id " + id + " found!"));
        registration.setStatus(RegistrationStatus.REJECTED);
        return this.registrationMapper.toDto(registration);
    }
}
