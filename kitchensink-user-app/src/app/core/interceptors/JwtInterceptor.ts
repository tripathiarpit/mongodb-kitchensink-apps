import { Injectable } from '@angular/core';
import {
  HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService'; // adjust path

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(
    private auth: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  private shouldBypassAuth(url: string): boolean {
    return [
      /\/api\/auth\/login$/,
      /\/api\/users\/register$/, // you had this in Spring Security permitAll
      /\/api\/auth\/forgot-password\/(request-otp|verify-otp|reset)$/ // forgot-password flow
    ].some((re) => re.test(url));
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let request = req;

    // Attach token unless the call is an auth/permitAll endpoint
    if (!this.shouldBypassAuth(req.url)) {
      const token = this.auth.getAuthToken?.();
      if (token) {
        request = req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        });
      }
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle expired/invalid session globally
        if (error.status === 401) {
          const message =
            error?.error?.message ||
            'Your session has expired. Please log in again.';

          // Clear token/session if you want to force re-login
          this.auth.logout?.();

          const ref = this.snackBar.open(message, 'Login', {
            duration: 5000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['snackbar-error']
          });

          // Redirect after 5s; if user clicks the action, go immediately
          const goLogin = () => this.router.navigate(['/login']);
          const timer = setTimeout(goLogin, 5000);
          ref.onAction().subscribe(() => {
            clearTimeout(timer);
            goLogin();
          });
        }

        return throwError(() => error);
      })
    );
  }
}
