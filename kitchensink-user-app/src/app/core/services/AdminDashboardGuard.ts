// src/app/core/services/AdminDashboardGuard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { AuthService } from './AuthService';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AdminDashboardGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    const roles = this.authService.getUserRole();
    if (!roles) {
      return of(this.router.createUrlTree(['/login']));
    }
    const userRoles: string[] = JSON.parse(roles);

    if (userRoles.includes('ADMIN')) {
      return of(this.router.createUrlTree(['/dashboard/admin']));
    } else {
      return of(this.router.createUrlTree(['/dashboard/user-details']));
    }
  }
}
