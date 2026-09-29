package ma.emi.backend.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.StudentRequestDto;
import ma.emi.backend.dto.response.StudentResponseDto;
import ma.emi.backend.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    public List<StudentResponseDto> getAllStudents(){
        return this.studentService.getAllStudents();
    }
    @GetMapping("/{id}")
    public StudentResponseDto getStudentById(@PathVariable Long id){
        return this.studentService.getStudentById(id);
    }

    @PostMapping
    public StudentResponseDto createStudent(@Valid @RequestBody StudentRequestDto request){
        return this.studentService.createStudent(request);
    }

    @PutMapping("/{id}")
    public StudentResponseDto updateStudent(@Valid @RequestBody StudentRequestDto request, @PathVariable Long id){
        return this.studentService.updateStudent(request,id);
    }

    @DeleteMapping("/{id}")
    public StudentResponseDto deleteStudent(@PathVariable Long id){
        return this.studentService.deleteStudent(id);
    }
}
