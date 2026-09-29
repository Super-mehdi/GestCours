package ma.emi.backend.mapper;


import ma.emi.backend.dto.request.CourseRequestDto;
import ma.emi.backend.dto.response.CourseResponseDto;
import ma.emi.backend.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequestDto request){
        return Course.builder()
                .name(request.name().trim())
                .description(request.description().trim())
                .code(request.code().trim().toUpperCase())
                .capacity(request.capacity())
                .build();
    }

    public CourseResponseDto toDto(Course entity){
        return new CourseResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.getCapacity()
        );
    }
}
