// src/app/core/services/AuthGaurd.ts

import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from './AuthService';
import {AppSnackbarComponent} from '../../shared/common-components/app-snackbar/app-snackbar';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router, private snackbar: MatSnackBar) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.authService.validateSession().pipe(
      map(isValid => {
        if (isValid) {
          // Session is valid, allow access to the route.
          return true;
        } else {
          this.snackbar.openFromComponent(AppSnackbarComponent, {
            data: { message: 'Your session has expired. Please log in again.' },
            duration: 4000,
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          });
          this.authService.logout();
         // this.router.navigate(['/login']);
          return this.router.createUrlTree(['/login']);
        }
      }),
      catchError(() => {
        // If the API call fails for any reason, assume no valid session.
        return of(this.router.createUrlTree(['/login']));
      })
    );
  }
}
