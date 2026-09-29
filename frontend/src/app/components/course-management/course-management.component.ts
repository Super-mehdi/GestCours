import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Course } from '../../models/course.model';
import { CourseService } from '../../services/course.service';
import { CourseDialogComponent } from '../course-dialog/course-dialog.component';

@Component({
  selector: 'app-course-management',
  templateUrl: './course-management.component.html',
  styleUrls: ['./course-management.component.scss']
})
export class CourseManagementComponent implements OnInit {
  courses: Course[] = [];
  displayedColumns: string[] = ['id', 'code', 'name', 'capacity', 'description', 'actions'];
  isLoading: boolean = false;

  constructor(
    private courseService: CourseService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.isLoading = true;
    this.courseService.getAllCourses().subscribe({
      next: (courses) => {
        this.courses = courses;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showFeedback('Failed to load courses', true);
      }
    });
  }

  openAddCourseDialog(): void {
    const dialogRef = this.dialog.open(CourseDialogComponent, {
      width: '450px',
      data: {}
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.courseService.createCourse(result).subscribe({
          next: () => {
            this.showFeedback('Course created successfully!');
            this.loadCourses();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to create course';
            this.showFeedback(msg, true);
          }
        });
      }
    });
  }

  openEditCourseDialog(course: Course): void {
    const dialogRef = this.dialog.open(CourseDialogComponent, {
      width: '450px',
      data: { course }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && course.id) {
        this.courseService.updateCourse(course.id, result).subscribe({
          next: () => {
            this.showFeedback('Course updated successfully!');
            this.loadCourses();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to update course';
            this.showFeedback(msg, true);
          }
        });
      }
    });
  }

  deleteCourse(course: Course): void {
    if (confirm(`Are you sure you want to delete course "${course.name}"?`)) {
      if (course.id) {
        this.courseService.deleteCourse(course.id).subscribe({
          next: () => {
            this.showFeedback('Course deleted successfully!');
            this.loadCourses();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Failed to delete course';
            this.showFeedback(msg, true);
          }
        });
      }
    }
  }

  private showFeedback(message: string, isError = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 3500,
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar']
    });
  }
}
