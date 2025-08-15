import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Injectable} from '@angular/core';
import {AuthService} from './AuthService';
import {Observable, of, tap} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class AccessGaurd implements CanActivate {
  constructor(private authService: AuthService, private router: Router, private snack: MatSnackBar) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): Observable<boolean> {
    const allowedRoles = route.data['allowedRoles'] as Array<string>;

    return this.authService.hasAccessToPage(allowedRoles).pipe(
      tap(hasAccess => {
        if (!hasAccess) {
          this.snack.open(
            "You are not authorized to access this. Contact Administrator",
            undefined,
            {duration: 30000, panelClass: ['snackbar-error']}
          );
          this.router.navigate(['/access-denied']); // fallback redirect
        }
      }),
      catchError(error => {
        console.error('Error checking access:', error);
        this.snack.open(
          "Error verifying access. Contact Administrator",
          undefined,
          {duration: 30000, panelClass: ['snackbar-error']}
        );
        this.router.navigate(['/access-denied']);
        return of(false);
      })
    );
  }
}
