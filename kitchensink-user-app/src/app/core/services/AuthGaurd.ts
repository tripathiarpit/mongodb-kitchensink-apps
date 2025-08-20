// src/app/core/guards/AuthGuard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { AuthService } from '../services/AuthService'; // Your authentication service
import { MatSnackBar } from '@angular/material/snack-bar';
import { AppSnackbarComponent } from '../../shared/common-components/app-snackbar/app-snackbar';
import { LoaderService } from '../services/LoaderService';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private loader: LoaderService
  ) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (state.url === '/login') {
      return true;
    }
    if (!this.authService.isLoggedIn()) {
      this.loader.hide();
      this.showMessage('You are not logged in. Please log in.');
      return this.router.createUrlTree(['/login']);
    }

    // If a token exists locally, now attempt to validate it with the backend.
    // This call will trigger your JwtInterceptor.
    return this.authService.validateSession().pipe( // Assuming validateSession makes an HTTP call
      map(isValid => {
        if (isValid) {
          console.log('Backend session validation successful.');
          return true; // Session is valid, allow access
        } else {
          // This path might be hit if validateSession returns false without an HTTP error (less common)
          this.loader.hide();
          this.authService.logout(); // Ensure local logout
          this.showMessage('Session invalid. Please log in again.');
          return this.router.createUrlTree(['/login']);
        }
      }),
      catchError(error => {
        // This catchError handles errors from validateSession() (e.g., network error)
        // For 401/session expiration, your JwtInterceptor's catchError will already
        // handle logout and redirect, and return EMPTY.
        // So, this catchError might primarily handle other types of errors or fallback.
        console.error('Error during session validation via AuthGuard:', error);

        // Check if the error was already handled by JwtInterceptor (e.g., via a 401)
        // If JwtInterceptor returned EMPTY, this catchError might not even be called,
        // or if it is, the user is already being redirected.
        // If not a 401 and an unhandled error, redirect to login
        this.loader.hide();
        this.authService.logout(); // Ensure local logout
        this.showMessage('Could not verify session. Please log in again.');
        return of(this.router.createUrlTree(['/login']));
      })
    );
  }

  private showMessage(message: string) {
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 4000,
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
