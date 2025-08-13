import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, throwError, BehaviorSubject} from 'rxjs';
import { catchError } from 'rxjs/operators';
import {Router} from '@angular/router';
import {ApiResponse} from '../../shared/model/ApiResponse';

export interface LoginResponse {
  token: string;
  email: string;
  username: string;
  roles: string[];
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

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      catchError(err => {
        const errorMsg = err?.error?.message || 'Login failed';
        return throwError(() => new Error(errorMsg));
      })
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

  sendOtp(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/request-otp`, { email });
  }

  verifyOtp(email: string, otp: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/verify-otp`, { email, otp });
  }

  resetPassword(email: string, newPassword: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/reset`, { email, newPassword });
  }
  saveUserData(res: LoginResponse) {
    localStorage.setItem('auth_token', res.token);
    localStorage.setItem('user_email', res.email);
    localStorage.setItem('username', res.username);
    localStorage.setItem('roles', JSON.stringify(res.roles));
  }
}
