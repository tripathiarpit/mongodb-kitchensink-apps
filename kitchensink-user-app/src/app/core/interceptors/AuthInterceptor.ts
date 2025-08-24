// src/app/core/services/AuthInterceptor.ts

import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError, BehaviorSubject, EMPTY } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AppSnackbarComponent } from '../../shared/common-components/app-snackbar/app-snackbar';
import { LoaderService } from '../services/LoaderService';
import { AuthService } from '../services/AuthService';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private router: Router,
    private snackBar: MatSnackBar,
    private loader: LoaderService,
    private authService: AuthService
  ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Attach the access token to the request
    const accessToken = this.authService.getAccessToken();
    if (accessToken) {
      request = this.addToken(request, accessToken);
    }

    return next.handle(request).pipe(
      catchError((error) => {
        // If refresh request itself fails, log out
        if (error instanceof HttpErrorResponse && error.status === 401 && request.url.includes('/api/auth/refresh')) {
          this.loader.hide();
          if(error.error.message =="Invalid email or password") {
            return this.handleAuthenticationError('Invalid email or password.');
          }
          return this.handleAuthenticationError('Session expired. Please log in again.');
        }

        // Handle 401 for other requests by attempting to refresh the token
        if (error instanceof HttpErrorResponse && error.status === 401) {
          if(error.error.message =="Invalid email or password") {
            return this.handleAuthenticationError(error.error.message);
          }
          this.loader.hide();
          return this.handle401Error(request, next);
        }

        // Handle 403 (Forbidden)
        if (error instanceof HttpErrorResponse && error.status === 403) {
          this.loader.hide();
          this.showMessage(error.error.message || 'Access denied.');
          this.router.navigate(['/access-denied']);
          return EMPTY; // Or throwError to propagate if you want components to handle
        }

        return throwError(() => error);
      })
    );
  }

  private addToken(request: HttpRequest<any>, token: string) {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // if (!this.isRefreshing) {
    //   this.isRefreshing = true;
    //   this.refreshTokenSubject.next(null); // Clear the subject
    //
    //   return this.authService.refreshToken().pipe(
    //     switchMap((tokens: any) => {
    //       this.isRefreshing = false;
    //       this.refreshTokenSubject.next(tokens.accessToken);
    //       return next.handle(this.addToken(request, tokens.accessToken));
    //     }),
    //     catchError((refreshError) => {
    //       this.isRefreshing = false;
    //       this.refreshTokenSubject.next(null);
    //       return this.logoutAndRedirect('Your session has expired. Please log in again.');
    //     })
    //   );
    // } else {
    //   return this.refreshTokenSubject.pipe(
    //     filter(token => token !== null),
    //     take(1),
    //     switchMap(token => {
    //       if (token === 'error') {
    //         return this.logoutAndRedirect('Your session has expired. Please log in again.');
    //       }
    //       return next.handle(this.addToken(request, token));
    //     }),
    //     catchError(() => {
    //       return this.logoutAndRedirect('An unexpected error occurred. Please log in again.');
    //     })
    //   );
    // }
    return this.logoutAndRedirect('Your session has expired. Please log in again.');
  }
  private handleAuthenticationError(message: string): Observable<never> {
    this.showMessage(message);
    this.authService.clearSessionStorage();
    this.router.navigate(['/login']);
    this.loader.hide();
    return throwError(() => new Error(message));
  }

  private showMessage(message: string) {
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 4000,
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
  private logoutAndRedirect(message: string): Observable<never> {
    this.authService.clearSessionStorage();
    this.showMessage(message);
    this.router.navigate(['/login']);
    return EMPTY;
  }
}
