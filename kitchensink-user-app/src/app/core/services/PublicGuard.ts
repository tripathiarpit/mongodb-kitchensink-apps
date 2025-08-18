// src/app/core/services/PublicGuard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from './AuthService';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class PublicGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.validateSession().pipe(
      map(isLoggedIn => {
        if (isLoggedIn) {
          return this.router.createUrlTree(['/dashboard']);
        }
        return true;
      })
    );
  }
}
