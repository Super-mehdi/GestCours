package ma.emi.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.RegistrationRequestDto;
import ma.emi.backend.dto.response.RegistrationResponseDto;
import ma.emi.backend.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @GetMapping("/{id}")
    public RegistrationResponseDto getRegistration(@PathVariable Long id){
        return this.registrationService.getRegistration(id);
    }
    @GetMapping
    public List<RegistrationResponseDto> getRegistrations(){
        return this.registrationService.getAllRegistrations();
    }

    @GetMapping("/student/{studentId}")
    public List<RegistrationResponseDto> getRegistrationsByStudent(@PathVariable Long studentId){
        return this.registrationService.getRegistrationsByStudentId(studentId);
    }

    @PostMapping
    public RegistrationResponseDto createRegistration(@Valid @RequestBody RegistrationRequestDto request){
        return this.registrationService.createRegistration(request);
    }

    @DeleteMapping("/{id}")
    public RegistrationResponseDto deleteRegistration(@PathVariable Long id){
        return this.registrationService.deleteRegistration(id);
    }


}
