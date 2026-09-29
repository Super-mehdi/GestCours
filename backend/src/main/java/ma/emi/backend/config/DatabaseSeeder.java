package ma.emi.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.emi.backend.entity.Course;
import ma.emi.backend.entity.Registration;
import ma.emi.backend.entity.RegistrationStatus;
import ma.emi.backend.entity.Student;
import ma.emi.backend.repository.CourseRepository;
import ma.emi.backend.repository.RegistrationRepository;
import ma.emi.backend.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RegistrationRepository registrationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (studentRepository.count() > 0 || courseRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial CSV seeding.");
            return;
        }

        log.info("Starting database seeding from CSV files...");

        // 1. Seed Students & track CSV id -> persisted Student mapping
        Map<Long, Student> studentMap = seedStudents();

        // 2. Seed Courses & track CSV id -> persisted Course mapping
        Map<Long, Course> courseMap = seedCourses();

        // 3. Seed Registrations linking persisted Students and Courses
        seedRegistrations(studentMap, courseMap);

        log.info("Database seeding successfully completed!");
    }

    private Map<Long, Student> seedStudents() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/students.csv");
        Map<Long, Student> studentMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().skip(1).filter(l -> !l.isBlank()).toList();
            for (String line : lines) {
                // Format: id,first_name,last_name,email
                String[] tokens = line.split(",");
                Long csvId = Long.parseLong(tokens[0].trim());
                Student student = Student.builder()
                        .firstName(tokens[1].trim())
                        .lastName(tokens[2].trim())
                        .email(tokens[3].trim())
                        .build();

                Student saved = studentRepository.save(student);
                studentMap.put(csvId, saved);
            }
        }
        log.info("Seeded {} students.", studentMap.size());
        return studentMap;
    }

    private Map<Long, Course> seedCourses() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/courses.csv");
        Map<Long, Course> courseMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().skip(1).filter(l -> !l.isBlank()).toList();
            for (String line : lines) {
                // Format: id,code,name,capacity
                String[] tokens = line.split(",");
                Long csvId = Long.parseLong(tokens[0].trim());
                String code = tokens[1].trim();
                String name = tokens[2].trim();
                Long capacity = Long.parseLong(tokens[3].trim());

                Course course = Course.builder()
                        .code(code)
                        .name(name)
                        .description("Course on " + name)
                        .capacity(capacity)
                        .build();

                Course saved = courseRepository.save(course);
                courseMap.put(csvId, saved);
            }
        }
        log.info("Seeded {} courses.", courseMap.size());
        return courseMap;
    }

    private void seedRegistrations(Map<Long, Student> studentMap, Map<Long, Course> courseMap) throws Exception {
        ClassPathResource resource = new ClassPathResource("data/registrations.csv");
        List<Registration> registrations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().skip(1).filter(l -> !l.isBlank()).toList();
            for (String line : lines) {
                // Format: id,student_id,course_id,registration_date,status
                String[] tokens = line.split(",");
                Long studentCsvId = Long.parseLong(tokens[1].trim());
                Long courseCsvId = Long.parseLong(tokens[2].trim());
                String statusStr = tokens.length > 4 ? tokens[4].trim() : "ACTIVE";

                Student student = studentMap.get(studentCsvId);
                Course course = courseMap.get(courseCsvId);

                if (student != null && course != null) {
                    Registration registration = Registration.builder()
                            .student(student)
                            .course(course)
                            .status(RegistrationStatus.valueOf(statusStr.toUpperCase()))
                            .build();
                    registrations.add(registration);
                } else {
                    log.warn("Skipping registration line: student {} or course {} not found in seed maps.", studentCsvId, courseCsvId);
                }
            }
        }
        registrationRepository.saveAll(registrations);
        log.info("Seeded {} registrations.", registrations.size());
    }
}

