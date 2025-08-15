import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import {Injectable} from '@angular/core';
import {AuthService} from './AuthService';
import {Observable, of, tap} from 'rxjs';
import {catchError} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    console.log('AuthGuard triggered for route:', route.routeConfig?.path);
    console.log('Full URL:', this.router.url);
    console.log('Route params:', route.params);

    return this.authService.validateSession().pipe(
      tap(isValid => {
        console.log('Session valid:', isValid);
        if (!isValid) {
          this.router.navigate(['/login']);
        }
      }),
      catchError(err => {
        console.log('AuthGuard error:', err);
        this.router.navigate(['/login']);
        return of(false);
      })
    );
  }
}

