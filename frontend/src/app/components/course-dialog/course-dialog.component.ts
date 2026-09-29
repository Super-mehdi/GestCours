import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Course } from '../../models/course.model';

@Component({
  selector: 'app-course-dialog',
  templateUrl: './course-dialog.component.html',
  styleUrls: ['./course-dialog.component.scss']
})
export class CourseDialogComponent {
  courseForm: FormGroup;
  isEditMode: boolean = false;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<CourseDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { course?: Course }
  ) {
    this.isEditMode = !!data?.course;
    this.courseForm = this.fb.group({
      name: [data?.course?.name || '', [Validators.required, Validators.maxLength(100)]],
      code: [data?.course?.code || '', [Validators.required, Validators.maxLength(20)]],
      description: [data?.course?.description || '', [Validators.required, Validators.maxLength(500)]],
      capacity: [data?.course?.capacity || 30, [Validators.required, Validators.min(1)]]
    });
  }

  onSubmit(): void {
    if (this.courseForm.valid) {
      this.dialogRef.close(this.courseForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
