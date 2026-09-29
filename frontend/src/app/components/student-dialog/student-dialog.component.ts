import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Student } from '../../models/student.model';

@Component({
  selector: 'app-student-dialog',
  templateUrl: './student-dialog.component.html',
  styleUrls: ['./student-dialog.component.scss']
})
export class StudentDialogComponent {
  studentForm: FormGroup;
  isEditMode: boolean = false;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<StudentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { student?: Student }
  ) {
    this.isEditMode = !!data?.student;
    this.studentForm = this.fb.group({
      firstName: [data?.student?.firstName || '', [Validators.required, Validators.maxLength(50)]],
      lastName: [data?.student?.lastName || '', [Validators.required, Validators.maxLength(50)]],
      email: [data?.student?.email || '', [Validators.required, Validators.email, Validators.maxLength(100)]]
    });
  }

  onSubmit(): void {
    if (this.studentForm.valid) {
      this.dialogRef.close(this.studentForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
