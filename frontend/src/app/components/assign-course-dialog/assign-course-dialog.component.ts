import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Course } from '../../models/course.model';
import { CourseService } from '../../services/course.service';
import { Student } from '../../models/student.model';
import { RegistrationStatus } from '../../models/registration.model';

@Component({
  selector: 'app-assign-course-dialog',
  templateUrl: './assign-course-dialog.component.html',
  styleUrls: ['./assign-course-dialog.component.scss']
})
export class AssignCourseDialogComponent implements OnInit {
  assignForm: FormGroup;
  availableCourses: Course[] = [];
  isLoadingCourses = true;

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    public dialogRef: MatDialogRef<AssignCourseDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { student: Student; alreadyAssignedCourseIds: number[] }
  ) {
    this.assignForm = this.fb.group({
      courseId: ['', Validators.required],
      registrationStatus: ['ACTIVE' as RegistrationStatus, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.isLoadingCourses = true;
    this.courseService.getAllCourses().subscribe({
      next: (courses) => {
        const assignedSet = new Set(this.data.alreadyAssignedCourseIds);
        this.availableCourses = courses.filter(c => c.id !== undefined && !assignedSet.has(c.id));
        this.isLoadingCourses = false;
      },
      error: () => {
        this.isLoadingCourses = false;
      }
    });
  }

  onSubmit(): void {
    if (this.assignForm.valid) {
      this.dialogRef.close({
        studentId: this.data.student.id,
        courseId: this.assignForm.value.courseId,
        registrationStatus: this.assignForm.value.registrationStatus
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
