package ma.emi.backend.mapper;

import ma.emi.backend.dto.request.StudentRequestDto;
import ma.emi.backend.dto.response.StudentResponseDto;
import ma.emi.backend.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public Student toEntity(StudentRequestDto request){
        return Student.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim())
                .build();
    }

    public StudentResponseDto toDto(Student entity){
        return new StudentResponseDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail()
        );
    }
}
