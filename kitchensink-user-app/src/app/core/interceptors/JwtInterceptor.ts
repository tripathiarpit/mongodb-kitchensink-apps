// src/app/core/interceptors/JwtInterceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, of, EMPTY } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService';
import { AppSnackbarComponent } from '../../shared/common-components/app-snackbar/app-snackbar';
import { LoaderService } from '../services/LoaderService';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private auth: AuthService,
    private snackBar: MatSnackBar,
    private router: Router,
    private loader: LoaderService
  ) {}

  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const accessToken = this.auth.getAuthToken();
    if (this.isPublicEndpoint(req.url)) {
      return next.handle(req);
    }
    const requestWithToken = accessToken ? this.addToken(req, accessToken) : req;
    return next.handle(requestWithToken).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          return this.handle401Error(requestWithToken, next);
        }
        return throwError(() => error);
      })
    );
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null); // Clear the subject to hold the new token

      return this.auth.refreshToken().pipe(
        switchMap((tokens: any) => {
          this.isRefreshing = false;
          this.refreshTokenSubject.next(tokens.accessToken);
          return next.handle(this.addToken(request, tokens.accessToken));
        }),
        catchError((refreshError) => {
          this.isRefreshing = false;
          return this.logoutAndRedirect('Your session has expired or another login has occurred. Please log in again.');
        })
      );
    } else {
      // If a refresh is already in progress, wait for it to complete
      return this.refreshTokenSubject.pipe(
        filter((token) => token !== null),
        take(1),
        switchMap((token) => next.handle(this.addToken(request, token)))
      );
    }
  }

  private logoutAndRedirect(message: string): Observable<never> {
    this.auth.clearSessionStorage();
    this.showMessage(message);
    this.router.navigate(['/login']);
    return EMPTY;
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

  private isPublicEndpoint(url: string): boolean {
    const publicPaths = [
      '/api/auth/login',
      '/api/users/register',
      '/api/auth/forgot-password/request-otp',
      '/api/auth/forgot-password/verify-otp',
      '/api/auth/forgot-password/reset',
      '/api/auth/account-verification/request-otp',
      '/api/auth/account-verification/verify-otp',
      '/api/auth/get-login-response-after-otp-verification',
      '/api/auth/refresh' // Crucial to allow the refresh token endpoint to bypass the interceptor's token check
    ];
    return publicPaths.some(path => url.includes(path)) || url.includes('restcountries.com');
  }
}
