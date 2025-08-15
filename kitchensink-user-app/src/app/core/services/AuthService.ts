import {EmbeddedViewRef, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, throwError, BehaviorSubject, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {Router} from '@angular/router';
import {ApiResponse} from '../../shared/model/ApiResponse';
import {MatSnackBar, MatSnackBarRef} from '@angular/material/snack-bar';

export interface LoginResponse {
  token: string;
  email: string;
  username: string;
  fullName: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'auth_token';
  private userRoleKey = 'roles';
  private fullnameKey ='fullName';
  private emailKey ='user_email';
  isLoggedIn$ = new BehaviorSubject<boolean>(false);
  private matref!: MatSnackBarRef<EmbeddedViewRef<any>>;
  constructor(private http: HttpClient, private router: Router, private snack: MatSnackBar) { }

  isAuthorized(allowedRoles: string[]): boolean {
    const roleStr = this.getUserRole();
    if (!roleStr) return false;
    const roles: string[] = JSON.parse(roleStr);
    return roles.some(r => allowedRoles.includes(r));
  }

  getUserRole(): string | null {
    return localStorage.getItem(this.userRoleKey);
  }
  hasAccessToPage(allowedRoles: string[]): Observable<boolean> {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) {
      this.snack.open(
        "You are not authorized to access this. Contact Administrator",
        undefined,
        { duration: 30000, panelClass: ['snackbar-error'] }
      );
      return of(false);
    }

    return this.getRolesFromToken(token).pipe(
      map(roles => {
        const hasAccess = roles.some(role => allowedRoles.includes(role));
        if (!hasAccess) {
          this.snack.open(
            "You are not authorized to access this. Contact Administrator",
            undefined,
            { duration: 30000, panelClass: ['snackbar-error'] }
          );
        }
        return hasAccess;
      })
    );
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
    localStorage.removeItem(this.fullnameKey);
    localStorage.removeItem(this.emailKey);
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
    localStorage.setItem(this.emailKey, res.email);
    localStorage.setItem('username', res.username);
    localStorage.setItem(this.fullnameKey, res.fullName);
    localStorage.setItem('roles', JSON.stringify(res.roles));
  }
  getFullName(): string | null {
    return localStorage.getItem(this.fullnameKey);
  }
  getEmail(): string | null {
    return localStorage.getItem(this.emailKey);
  }
  getRolesFromToken(authToken: string | null): Observable<string[]> {
    if (!authToken) {
      return of([]); // return empty array if token is null
    }

    const headers = { Authorization: `Bearer ${authToken}` };
    return this.http.get<string[]>('/api/auth/get-roles-by-token', { headers }).pipe(
      catchError(err => {
        console.error('Error fetching roles:', err);
        return of([]);
      })
    );
  }

  getRolesByEmail(email: string) {
    return this.http.get<string[]>(`/api/auth/roles-by-email/${email}`);
  }


  validateSession(): Observable<boolean> {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) return of(false);

    const headers = { Authorization: `Bearer ${token}` };
    return this.http.get<boolean>('/api/auth/validate-session', { headers }).pipe(
      catchError(err => of(false))
    );
  }



}
