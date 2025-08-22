// src/app/core/interceptors/JwtInterceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, EMPTY, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService';
import { AppSnackbarComponent } from '../../shared/common-components/app-snackbar/app-snackbar';
import { LoaderService } from '../services/LoaderService';
import { SessionService } from '../services/SessionService';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private isLoggingOut = false; // prevent multiple logouts

  constructor(
    private auth: AuthService,
    private snackBar: MatSnackBar,
    private router: Router,
    private loader: LoaderService,
    private sessionService: SessionService
  ) {}

  private shouldBypassAuth(url: string): boolean {
    return [
      '/api/auth/login',
      '/api/users/register',
      '/api/auth/forgot-password/request-otp',
      '/api/auth/forgot-password/verify-otp',
      '/api/auth/forgot-password/reset',
      '/api/auth/account-verification/request-otp',
      '/api/auth/account-verification/verify-otp',
      '/api/auth/account-verification/reset',
      '/api/auth/get-login-response-after-otp-verification',
      '/api/auth/logout',
      '/api/auth/validate-session'
    ].some((path) => url.includes(path) || url.includes('restcountries.com'));
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // If a request shouldn't be authenticated, handle it directly.
    if (this.shouldBypassAuth(req.url)) {
      return next.handle(req);
    }

    // Check if the session is active before proceeding.
    return this.sessionService.isSessionActive().pipe(
      switchMap(isSessionActive => {
        if (!isSessionActive) {
          if (!this.isLoggingOut) {
            this.isLoggingOut = true;
            this.auth.clearSessionStorage();
            this.showMessage('Your session has expired. Please log in again.');
            this.router.navigate(['/login']);
          }
          return EMPTY; // Stop the request
        }

        // If session is active, clone the request with the token.
        const token = this.auth.getAuthToken();
        const requestWithToken = req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        });

        // Handle the response after the request has been made.
        return next.handle(requestWithToken).pipe(
          catchError((error: HttpErrorResponse) => {
            const isSessionExpiredError =
              (error.status === 401) ||
              (error.status === 400 && error.error?.message?.includes("Session has been expired"));

            if (isSessionExpiredError && !this.isLoggingOut) {
              this.loader.hide();
              this.isLoggingOut = true;
              this.auth.clearSessionStorage();
              this.showMessage('Your session has expired. Please log in again.');
              this.router.navigate(['/login']);
              return EMPTY;
            }

            return throwError(() => error);
          })
        );
      })
    );
  }

  private showMessage(message: string): void {
    this.loader.hide();
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 4000,
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
