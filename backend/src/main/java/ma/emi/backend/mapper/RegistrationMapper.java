package ma.emi.backend.mapper;

import ma.emi.backend.dto.request.RegistrationRequestDto;
import ma.emi.backend.dto.response.RegistrationResponseDto;
import ma.emi.backend.entity.Course;
import ma.emi.backend.entity.Registration;
import ma.emi.backend.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    public Registration toEntity(RegistrationRequestDto request, Course course, Student student){
        return Registration.builder()
                .student(student)
                .course(course)
                .status(request.registrationStatus())
                .build();
    }

    public RegistrationResponseDto toDto(Registration registration){
        return new RegistrationResponseDto(
                registration.getId(),
                registration.getStudent().getId(),
                registration.getCourse().getId(),
                registration.getStatus()
        );
    }
}
