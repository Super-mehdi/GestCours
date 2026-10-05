import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { StudentListComponent } from './components/student-list/student-list.component';
import { CourseManagementComponent } from './components/course-management/course-management.component';
import { AdminRequestsComponent } from './components/admin-requests/admin-requests.component';
import { StudentPortalComponent } from './components/student-portal/student-portal.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },

  // Admin Routes (Protected by AuthGuard & RoleGuard)
  {
    path: 'students',
    component: StudentListComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'ROLE_ADMIN' }
  },
  {
    path: 'courses',
    component: CourseManagementComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'ROLE_ADMIN' }
  },
  {
    path: 'requests',
    component: AdminRequestsComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'ROLE_ADMIN' }
  },

  // Student Routes (Protected by AuthGuard & RoleGuard)
  {
    path: 'my-courses',
    component: StudentPortalComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'ROLE_STUDENT' }
  },

  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
