import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router, private snackBar: MatSnackBar) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 403) {
          this.snackBar.open(error.error, 'Close', {
            duration: 10000,
            panelClass: ['snack-bar-error']
          });
          this.router.navigate(['/access-denied']);
          return throwError(() => error);
        }

        if (error.status === 401) {
          this.snackBar.open(error.error, 'Close', {
            duration: 10000,
            panelClass: ['snack-bar-error']
          });
          this.router.navigate(['/login']);
          return throwError(() => error);
        }

        // For other errors, let them bubble up
        return throwError(() => error);
      })
    );
  }
}
