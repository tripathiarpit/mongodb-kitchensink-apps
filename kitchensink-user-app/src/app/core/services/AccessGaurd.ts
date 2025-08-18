// src/app/core/services/AccessGaurd.ts

import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from './AuthService';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccessGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    const allowedRoles = route.data['allowedRoles'] as string[];

    // If no roles are defined on the route, allow access (or adjust as needed)
    if (!allowedRoles || allowedRoles.length === 0) {
      return of(true);
    }

    // Check if the logged-in user has the required roles
    return this.authService.hasAccessToPage(allowedRoles).pipe(
      map(hasAccess => {
        if (hasAccess) {
          return true;
        } else {
          this.snack.open(
            "You are not authorized to access this. Contact Administrator",
            undefined,
            { duration: 3000, panelClass: ['snackbar-error'] }
          );
          return this.router.createUrlTree(['/access-denied']);
        }
      }),
      catchError(() => {
        // In case of an API error getting roles, deny access
        this.snack.open(
          "Error verifying access. Contact Administrator",
          undefined,
          { duration: 3000, panelClass: ['snackbar-error'] }
        );
        return of(this.router.createUrlTree(['/access-denied']));
      })
    );
  }
}
