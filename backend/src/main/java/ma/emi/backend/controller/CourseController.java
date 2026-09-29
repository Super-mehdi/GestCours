package ma.emi.backend.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.CourseRequestDto;
import ma.emi.backend.dto.response.CourseResponseDto;
import ma.emi.backend.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public List<CourseResponseDto> getAllCourses(){
        return this.courseService.getAllCourses();
    }
    @GetMapping("/{id}")
    public CourseResponseDto getCourse(@PathVariable Long id){
        return this.courseService.getCourseById(id);
    }

    @PostMapping
    public CourseResponseDto createCourse(@Valid @RequestBody CourseRequestDto courseRequestDto){
        return this.courseService.createCourse(courseRequestDto);
    }

    @PutMapping("/{id}")
    public CourseResponseDto updateCourse(@Valid @RequestBody CourseRequestDto request, @PathVariable Long id){
        return this.courseService.updateCourse(request,id);
    }

    @DeleteMapping("/{id}")
    public CourseResponseDto deleteCourse(@PathVariable Long id){
        return this.courseService.deleteCourse(id);
    }

}
