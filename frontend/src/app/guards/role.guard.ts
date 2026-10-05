import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const expectedRole = route.data['role'];

    if (!this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/login']);
    }

    const currentRole = this.authService.getCurrentUser()?.role;
    if (currentRole === expectedRole) {
      return true;
    }

    // Role mismatch: redirect appropriately
    if (currentRole === 'ROLE_ADMIN') {
      return this.router.createUrlTree(['/students']);
    } else {
      return this.router.createUrlTree(['/my-courses']);
    }
  }
}
