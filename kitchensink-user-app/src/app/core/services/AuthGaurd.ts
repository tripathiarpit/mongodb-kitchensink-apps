// src/app/core/services/AuthGaurd.ts

import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from './AuthService';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    // Check if the session is valid via an API call
    return this.authService.validateSession().pipe(
      map(isValid => {
        if (isValid) {
          // Session is valid, allow access to the route.
          return true;
        } else {
          // Session is not valid, redirect to login page.
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
