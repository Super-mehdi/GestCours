import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { Student } from '../../models/student.model';
import { Course } from '../../models/course.model';
import { Registration, RegistrationRequest } from '../../models/registration.model';
import { StudentService } from '../../services/student.service';
import { CourseService } from '../../services/course.service';
import { RegistrationService } from '../../services/registration.service';
import { StudentDialogComponent } from '../student-dialog/student-dialog.component';
import { AssignCourseDialogComponent } from '../assign-course-dialog/assign-course-dialog.component';

interface EnrolledCourseInfo {
  registrationId: number;
  course: Course;
  status: string;
}

@Component({
  selector: 'app-student-list',
  templateUrl: './student-list.component.html',
  styleUrls: ['./student-list.component.scss']
})
export class StudentListComponent implements OnInit {
  students: Student[] = [];
  coursesMap: Map<number, Course> = new Map();
  studentRegistrations: Map<number, EnrolledCourseInfo[]> = new Map();
  expandedStudentId: number | null = null;
  isLoading: boolean = false;
  searchFilter: string = '';

  displayedColumns: string[] = ['name', 'email', 'enrolledCount', 'actions'];

  constructor(
    private studentService: StudentService,
    private courseService: CourseService,
    private registrationService: RegistrationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.refreshAllData();
  }

  refreshAllData(): void {
    this.isLoading = true;
    forkJoin({
      students: this.studentService.getAllStudents(),
      courses: this.courseService.getAllCourses(),
      registrations: this.registrationService.getAllRegistrations()
    }).subscribe({
      next: ({ students, courses, registrations }) => {
        this.students = students;

        this.coursesMap = new Map();
        courses.forEach(c => {
          if (c.id !== undefined) {
            this.coursesMap.set(c.id, c);
          }
        });

        this.buildRegistrationsMap(registrations);
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.showFeedback('Failed to load students and courses data', true);
      }
    });
  }

  private buildRegistrationsMap(registrations: Registration[]): void {
    this.studentRegistrations = new Map();
    registrations.forEach(r => {
      const course = this.coursesMap.get(r.courseId) || {
        id: r.courseId,
        code: 'N/A',
        name: `Course #${r.courseId}`,
        description: 'Details unavailable',
        capacity: 0
      };

      const list = this.studentRegistrations.get(r.studentId) || [];
      list.push({
        registrationId: r.id!,
        course: course,
        status: r.status
      });
      this.studentRegistrations.set(r.studentId, list);
    });
  }

  toggleExpand(student: Student): void {
    if (student.id) {
      this.expandedStudentId = this.expandedStudentId === student.id ? null : student.id;
    }
  }

  getEnrolledCourses(studentId?: number): EnrolledCourseInfo[] {
    if (!studentId) return [];
    return this.studentRegistrations.get(studentId) || [];
  }

  get filteredStudents(): Student[] {
    if (!this.searchFilter.trim()) {
      return this.students;
    }
    const q = this.searchFilter.toLowerCase();
    return this.students.filter(s =>
      s.firstName.toLowerCase().includes(q) ||
      s.lastName.toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q)
    );
  }

  openAddStudentDialog(): void {
    const dialogRef = this.dialog.open(StudentDialogComponent, {
      width: '450px',
      data: {}
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.studentService.createStudent(result).subscribe({
          next: () => {
            this.showFeedback('Student registered successfully!');
            this.refreshAllData();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to register student';
            this.showFeedback(msg, true);
          }
        });
      }
    });
  }

  openEditStudentDialog(student: Student): void {
    const dialogRef = this.dialog.open(StudentDialogComponent, {
      width: '450px',
      data: { student }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && student.id) {
        this.studentService.updateStudent(student.id, result).subscribe({
          next: () => {
            this.showFeedback('Student details updated!');
            this.refreshAllData();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to update student';
            this.showFeedback(msg, true);
          }
        });
      }
    });
  }

  deleteStudent(student: Student): void {
    if (confirm(`Are you sure you want to delete ${student.firstName} ${student.lastName}?`)) {
      if (student.id) {
        this.studentService.deleteStudent(student.id).subscribe({
          next: () => {
            this.showFeedback('Student deleted successfully!');
            this.refreshAllData();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to delete student';
            this.showFeedback(msg, true);
          }
        });
      }
    }
  }

  openAssignCourseDialog(student: Student): void {
    const enrolled = this.getEnrolledCourses(student.id);
    const assignedIds = enrolled.map(e => e.course.id!).filter(id => id !== undefined);

    const dialogRef = this.dialog.open(AssignCourseDialogComponent, {
      width: '500px',
      data: {
        student,
        alreadyAssignedCourseIds: assignedIds
      }
    });

    dialogRef.afterClosed().subscribe((result: RegistrationRequest) => {
      if (result) {
        this.registrationService.createRegistration(result).subscribe({
          next: () => {
            this.showFeedback('Course successfully assigned to student!');
            this.refreshAllData();
            this.expandedStudentId = student.id!;
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to assign course';
            this.showFeedback(msg, true);
          }
        });
      }
    });
  }

  unregisterCourse(regInfo: EnrolledCourseInfo, student: Student): void {
    if (confirm(`Unregister ${student.firstName} from "${regInfo.course.name}"?`)) {
      this.registrationService.deleteRegistration(regInfo.registrationId).subscribe({
        next: () => {
          this.showFeedback('Course unregistered successfully!');
          this.refreshAllData();
        },
        error: (err) => {
          const msg = typeof err.error === 'string' ? err.error : 'Failed to unregister course';
          this.showFeedback(msg, true);
        }
      });
    }
  }

  private showFeedback(message: string, isError = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 3500,
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar']
    });
  }
}
