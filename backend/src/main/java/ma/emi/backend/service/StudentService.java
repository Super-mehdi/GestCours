package ma.emi.backend.service;

import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.StudentRequestDto;
import ma.emi.backend.dto.response.StudentResponseDto;
import ma.emi.backend.entity.Student;
import ma.emi.backend.exception.DuplicateStudentException;
import ma.emi.backend.exception.StudentNotFoundException;
import ma.emi.backend.mapper.StudentMapper;
import ma.emi.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper  studentMapper;

    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto) {
        Student student =  studentMapper.toEntity(studentRequestDto);
        if (this.studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateStudentException("A student with this email already exists");
        }
        Student savedStudent = this.studentRepository.save(student);
        return studentMapper.toDto(savedStudent);
    }

    public StudentResponseDto getStudentById(Long id) {
        return this.studentRepository.findById(id).map(studentMapper::toDto).orElseThrow(() -> new StudentNotFoundException("A student with the id {%d} is not found".formatted(id)));
    }

    public List<StudentResponseDto> getAllStudents() {
        List<Student> students = this.studentRepository.findAll();
        return students.stream().map(this.studentMapper::toDto).toList();
    }

    @Transactional
    public StudentResponseDto updateStudent(StudentRequestDto studentRequestDto, Long id) {
        Student student = this.studentRepository.findById(id).orElseThrow(()->new StudentNotFoundException("A student with the id {%d} is not found".formatted(id)));
        student.setFirstName(studentRequestDto.firstName().trim());
        student.setLastName(studentRequestDto.lastName().trim());
        student.setEmail(studentRequestDto.email().trim());
        return  this.studentMapper.toDto(student);
    }

    @Transactional
    public StudentResponseDto deleteStudent(Long id) {
        Student student = this.studentRepository.findById(id).orElseThrow(()->new StudentNotFoundException("A student with the id {%d} is not found".formatted(id)));
        this.studentRepository.delete(student);
        return studentMapper.toDto(student);
    }

}
