import {EmbeddedViewRef, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, throwError, BehaviorSubject, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {Router} from '@angular/router';
import {ApiResponse} from '../../shared/model/ApiResponse';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AppSnackbarComponent} from '../../shared/common-components/app-snackbar/app-snackbar';

export interface LoginResponse {
  token: string;
  email: string;
  username: string;
  fullName: string;
  accountVerificationPending: boolean;
  firstLogin: boolean;
  response:boolean;
  roles: string[];
}
export interface DeleteResponse {
  success: boolean;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'auth_token';
  private userRoleKey = 'roles';
  private fullnameKey ='fullName';
  private emailKey ='user_email';
  isLoggedIn$ = new BehaviorSubject<boolean>(false);
  constructor(private http: HttpClient, private router: Router, private snackBar: MatSnackBar) { }

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
      this.snackBar.open(
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
          this.snackBar.open(
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
  logoutAndInvalidateToken(email: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/ldsdsogout', { email }).pipe(
      catchError(err => {
        const errorMsg = err?.error?.message || 'logout failed';
        return throwError(() => new Error(errorMsg));
      })
    );
  }

  logout(): void {
    let email= localStorage.getItem(this.emailKey as string);
    this.logoutAndInvalidateToken(email as string).subscribe({
      next: (response) => {
        this.showMessage("You Are Successfully signed out");
      },
      error: (error) => {
        throwError(() => new Error(error));
      }
    });
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userRoleKey);
    localStorage.removeItem(this.fullnameKey);
    localStorage.removeItem(this.emailKey);
    sessionStorage.clear();
    this.isLoggedIn$.next(false);
    this.router.navigate(['/login']);
  }
  showMessage(message: string) {
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 4000,
      verticalPosition: 'top', // position at the top
      panelClass: ['error-snackbar'] // custom CSS
    });
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
      return 'USER';
    } catch {
      return 'USER';
    }
  }

  requestForgotPasswordOtp(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/request-otp`, { email });
  }

  verifyForgotPasswordOtp(email: string, otp: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/verify-otp`, { email, otp });
  }
  verifyAccountVerificationOtp(email: string, otp: string): Observable<ApiResponse> {
    const payload = { email, otp }; // matches VerifyOtpRequest DTO
    return this.http.post<ApiResponse>(`/api/auth/account-verification/verify-otp`, payload);
  }
  requestAccountVerificationOtp(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/account-verification/request-otp`, { email });
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
      return of([]);
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

  fetchLoginResponseAfterOtpVerification(email: string): Observable<LoginResponse> {
    return this.http.get<LoginResponse>(`/api/auth/get-login-response-after-otp-verification`,
      {
        headers: { email }
      });
  }


}
