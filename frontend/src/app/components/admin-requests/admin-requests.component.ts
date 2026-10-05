import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RegistrationService } from '../../services/registration.service';
import { RegistrationDetail } from '../../models/registration.model';

@Component({
  selector: 'app-admin-requests',
  templateUrl: './admin-requests.component.html',
  styleUrls: ['./admin-requests.component.scss']
})
export class AdminRequestsComponent implements OnInit {
  pendingRequests: RegistrationDetail[] = [];
  displayedColumns: string[] = ['student', 'course', 'status', 'actions'];
  isLoading = false;

  constructor(
    private registrationService: RegistrationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadPendingRequests();
  }

  loadPendingRequests(): void {
    this.isLoading = true;
    this.registrationService.getPendingRegistrations().subscribe({
      next: (requests) => {
        this.pendingRequests = requests;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load pending requests', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  approve(request: RegistrationDetail): void {
    this.registrationService.approveRegistration(request.id).subscribe({
      next: () => {
        this.snackBar.open(`Confirmed enrollment for ${request.studentName} in ${request.courseName}!`, 'Dismiss', { duration: 3500 });
        this.loadPendingRequests();
      },
      error: () => {
        this.snackBar.open('Failed to approve request', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  reject(request: RegistrationDetail): void {
    if (confirm(`Reject enrollment request from ${request.studentName} for ${request.courseName}?`)) {
      this.registrationService.rejectRegistration(request.id).subscribe({
        next: () => {
          this.snackBar.open(`Request rejected for ${request.studentName}.`, 'Dismiss', { duration: 3500 });
          this.loadPendingRequests();
        },
        error: () => {
          this.snackBar.open('Failed to reject request', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      });
    }
  }
}
