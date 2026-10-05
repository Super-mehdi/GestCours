import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { CourseService } from '../../services/course.service';
import { RegistrationService } from '../../services/registration.service';
import { Course } from '../../models/course.model';
import { Registration } from '../../models/registration.model';

interface StudentEnrollmentView {
  registrationId?: number;
  course: Course;
  status: string;
}

@Component({
  selector: 'app-student-portal',
  templateUrl: './student-portal.component.html',
  styleUrls: ['./student-portal.component.scss']
})
export class StudentPortalComponent implements OnInit {
  studentName = '';
  studentId: number | null = null;
  isLoading = false;

  myEnrollments: StudentEnrollmentView[] = [];
  availableCourses: Course[] = [];

  constructor(
    private authService: AuthService,
    private courseService: CourseService,
    private registrationService: RegistrationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.studentName = user?.studentName || 'Student';
    this.studentId = user?.studentId || null;

    if (this.studentId) {
      this.loadStudentData();
    }
  }

  loadStudentData(): void {
    if (!this.studentId) return;
    this.isLoading = true;

    forkJoin({
      courses: this.courseService.getAllCourses(),
      registrations: this.registrationService.getRegistrationsByStudentId(this.studentId)
    }).subscribe({
      next: ({ courses, registrations }) => {
        const coursesMap = new Map<number, Course>();
        courses.forEach(c => {
          if (c.id !== undefined) coursesMap.set(c.id, c);
        });

        // Build list of student's current registrations (ACTIVE, PENDING, REJECTED)
        const enrolledCourseIds = new Set<number>();
        this.myEnrollments = registrations.map(reg => {
          enrolledCourseIds.add(reg.courseId);
          const course = coursesMap.get(reg.courseId) || {
            id: reg.courseId,
            code: 'N/A',
            name: `Course #${reg.courseId}`,
            description: '',
            capacity: 0
          };
          const resolvedStatus = (reg.registrationStatus || reg.status || 'ACTIVE') as string;
          return {
            registrationId: reg.id,
            course,
            status: resolvedStatus
          };
        });

        // Filter available courses that student is NOT enrolled in or pending
        this.availableCourses = courses.filter(c => c.id !== undefined && !enrolledCourseIds.has(c.id));
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load courses', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  requestEnrollment(course: Course): void {
    if (!this.studentId || !course.id) return;

    this.registrationService.requestCourseEnrollment(this.studentId, course.id).subscribe({
      next: () => {
        this.snackBar.open(`Enrollment requested for ${course.name}! Waiting for Admin confirmation.`, 'Dismiss', { duration: 4000 });
        this.loadStudentData();
      },
      error: (err) => {
        const msg = typeof err.error === 'string' ? err.error : 'Failed to request enrollment';
        this.snackBar.open(msg, 'Close', { duration: 4000, panelClass: ['error-snackbar'] });
      }
    });
  }

  get activeEnrollments(): StudentEnrollmentView[] {
    return this.myEnrollments.filter(e => e.status === 'ACTIVE');
  }

  get pendingEnrollments(): StudentEnrollmentView[] {
    return this.myEnrollments.filter(e => e.status === 'PENDING');
  }

  get otherEnrollments(): StudentEnrollmentView[] {
    return this.myEnrollments.filter(e => e.status === 'REJECTED' || e.status === 'INACTIVE');
  }
}
