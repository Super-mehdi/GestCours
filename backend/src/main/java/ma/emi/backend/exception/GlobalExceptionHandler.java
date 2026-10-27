package ma.emi.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({CourseNotFoundException.class, StudentNotFoundException.class, RegistrationNotFoundException.class})
    ResponseEntity<?> handleNotFoundException(Exception ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({DuplicateStudentException.class,DuplicateCourseException.class,DuplicateRegistrationException.class})
    ResponseEntity<?> handleDuplicateException(Exception ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(SimulatedCrashException.class)
    ResponseEntity<?> handleSimulatedCrash(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
