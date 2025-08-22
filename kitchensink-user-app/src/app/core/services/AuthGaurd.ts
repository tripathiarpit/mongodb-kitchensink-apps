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
    state: RouterStateSnapshot): Observable<boolean | UrlTree> {
    if (state.url === '/login') {
      return of(true);
    }
    if (!this.authService.isLoggedIn()) {
      this.loader.hide();
      this.showMessage('You are not logged in. Please log in.');
      return of(this.router.createUrlTree(['/login']));
    }

    return this.authService.isSessionActive().pipe(
      map(isActive => {
        if (isActive) {
          console.log('Backend session validation successful.');
          return true;
        } else {
          this.loader.hide();
          this.authService.clearSessionStorage();
          this.showMessage('Session invalid. Please log in again.');
          return this.router.createUrlTree(['/login']);
        }
      }),
      catchError((err) => {
        this.loader.hide();
        this.authService.clearSessionStorage();
        this.showMessage('An unexpected error occurred. Please log in again.');
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
