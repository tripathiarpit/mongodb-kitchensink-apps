import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, EMPTY, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AppSnackbarComponent } from '../../shared/common-components/app-snackbar/app-snackbar';
import {LoaderService} from '../services/LoaderService';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router, private snackBar: MatSnackBar, private loader: LoaderService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 403) {
          this.showMessage(error.error.message || 'Access denied.');
          this.router.navigate(['/access-denied']);
          return EMPTY; // stop propagation to component
        }

        if (error.status === 401) {
          this.showMessage(error.error.message || 'Session expired. Please login again.');
          this.router.navigate(['/login']);
          return EMPTY; // stop propagation to component
        }

        // propagate other errors to component
        return throwError(() => error);
      })
    );
  }

  private showMessage(message: string) {
    this.loader.hide();
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 4000,
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
