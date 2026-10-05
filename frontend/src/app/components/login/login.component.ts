import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { TestCredential } from '../../models/user.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  isLoading = false;
  testCredentials: TestCredential[] = [];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.redirectUser();
    }
    this.loadTestCredentials();
  }

  loadTestCredentials(): void {
    this.authService.getTestCredentials().subscribe({
      next: (creds) => this.testCredentials = creds,
      error: () => {
        // Fallback demo credentials if backend seeder is starting up
        this.testCredentials = [
          {
            label: 'Admin Account',
            email: 'admin@portal.com',
            password: 'admin123',
            role: 'ROLE_ADMIN',
            description: 'Full administrative access'
          },
          {
            label: 'Student (Youssef)',
            email: 'youssef.elamrani@example.com',
            password: 'student123',
            role: 'ROLE_STUDENT',
            description: 'Student portal access'
          }
        ];
      }
    });
  }

  quickFill(cred: TestCredential): void {
    this.loginForm.patchValue({
      email: cred.email,
      password: cred.password
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open(`Welcome, ${response.studentName}!`, 'Dismiss', { duration: 3000 });
        this.redirectUser();
      },
      error: (err) => {
        this.isLoading = false;
        const msg = typeof err.error === 'string' ? err.error : 'Invalid credentials';
        this.snackBar.open(msg, 'Close', { duration: 4000, panelClass: ['error-snackbar'] });
      }
    });
  }

  private redirectUser(): void {
    if (this.authService.isAdmin()) {
      this.router.navigate(['/students']);
    } else {
      this.router.navigate(['/my-courses']);
    }
  }
}
