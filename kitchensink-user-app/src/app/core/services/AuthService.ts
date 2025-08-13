import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {Observable, of, throwError, tap, BehaviorSubject} from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {Router} from '@angular/router';

interface LoginResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'auth_token';
  private userRoleKey = 'user_role';
  isLoggedIn$ = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient, private router: Router) { }

  getUserRole(): string | null {
    return localStorage.getItem(this.userRoleKey);
  }

  isAuthorized(allowedRoles: string[]): boolean {
    const role = this.getUserRole();
    return role !== null && allowedRoles.includes(role);
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.tokenKey);
    // Optionally, check token expiration here
    return !!token;
  }

  login(email: string, password: string): Observable<boolean> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.token);
        const role = this.parseRoleFromToken(response.token);
        localStorage.setItem(this.userRoleKey, role);
        this.isLoggedIn$.next(true);
      }),
      map(() => true),
      catchError(err => throwError(() => new Error(err.error || 'Login failed')))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userRoleKey);
    sessionStorage.clear();
    this.isLoggedIn$.next(false);
    this.router.navigate(['/login']);
  }

  getAuthToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private parseRoleFromToken(token: string): string {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      // assuming your JWT has roles in 'roles' or 'role' claim
      if (payload.roles && Array.isArray(payload.roles)) {
        return payload.roles[0]; // return first role for simplicity
      }
      if (payload.role) {
        return payload.role;
      }
      return 'USER'; // fallback
    } catch {
      return 'USER';
    }
  }
}
