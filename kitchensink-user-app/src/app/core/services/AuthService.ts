import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, of } from 'rxjs';
import { catchError, map, finalize } from 'rxjs/operators'; // Import finalize for cleanup
import { Router } from '@angular/router';
import { ApiResponse } from '../../shared/model/ApiResponse';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AppSnackbarComponent } from '../../shared/common-components/app-snackbar/app-snackbar';
import {LoaderService} from './LoaderService';

export interface LoginResponse {
  token: string;
  email: string;
  username: string;
  fullName: string;
  accountVerificationPending: boolean;
  firstLogin: boolean;
  response: boolean;
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
  private fullnameKey = 'fullName';
  private emailKey = 'user_email';
  isLoggedIn$ = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient, private router: Router, private snackBar: MatSnackBar,private loader: LoaderService) { }

  /**
   * Checks if the current user is authorized based on roles stored in local storage.
   * Note: This relies on local storage roles which might be stale. For real-time checks,
   * use `hasAccessToPage()`.
   * @param allowedRoles Array of roles that are permitted to access.
   * @returns True if the user has any of the allowed roles, false otherwise.
   */
  isAuthorized(allowedRoles: string[]): boolean {
    const roleStr = this.getUserRole();
    if (!roleStr) return false;
    const roles: string[] = JSON.parse(roleStr);
    return roles.some(r => allowedRoles.includes(r));
  }

  /**
   * Retrieves the user roles string from local storage.
   * @returns The JSON string of roles or null if not found.
   */
  getUserRole(): string | null {
    return localStorage.getItem(this.userRoleKey);
  }
  getDashboardUrl(): string {
    const rolesString = this.getUserRole();
    const roles: string[] = rolesString ? JSON.parse(rolesString) : [];

    if (roles.includes('ADMIN')) {
      return '/dashboard/admin';
    } else {
      window.location.reload();
      return '/dashboard/user-details';
    }
  }
  /**
   * Checks user access to a page by fetching roles from the backend using the current token.
   * This provides a more real-time validation than checking local storage directly.
   * @param allowedRoles Array of roles that are permitted to access.
   * @returns An Observable<boolean> indicating if the user has access.
   */
  hasAccessToPage(allowedRoles: string[]): Observable<boolean> {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) {
      // If no token, the user is not authenticated, thus no access.
      return of(false);
    }

    // Fetch roles from the token via an API call
    return this.getRolesFromToken(token).pipe(
      map(roles => {
        // Check if any of the user's roles are in the allowedRoles list
        const hasAccess = roles.some(role => allowedRoles.includes(role));
        // IMPORTANT: Snackbar message removed from here. The AccessGuard
        // will now be responsible for displaying the "not authorized" message.
        return hasAccess;
      }),
      catchError(err => {
        // Log the error but return false to deny access on API failure
        console.error('Error fetching roles for access check:', err);
        return of(false);
      })
    );
  }

  /**
   * Checks if a user is currently logged in based on the presence of an auth token.
   * @returns True if a token exists, false otherwise.
   */
  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.tokenKey);
    return !!token;
  }

  /**
   * Handles user login by making an HTTP POST request.
   * @param email User's email.
   * @param password User's password.
   * @returns An Observable of the LoginResponse.
   */
  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      catchError(err => {
        const errorMsg = err?.error?.message || 'Login failed. Please check your credentials.';
        return throwError(() => new Error(errorMsg));
      })
    );
  }

  /**
   * Makes an HTTP POST request to invalidate the user's token on the backend.
   * @param email User's email.
   * @returns An Observable of the LoginResponse (or a generic success response).
   */
  logoutAndInvalidateToken(email: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/logout', { email }).pipe(
      catchError(err => {
        // Log the error but don't rethrow, so the `finalize` block still runs.
        console.error('Logout API failed:', err);
        const errorMsg = err?.error?.message || 'Logout failed on the server.';
        this.showMessage(errorMsg); // Inform the user about the API error
        return of({} as LoginResponse); // Return an empty/dummy response to complete the observable
      })
    );
  }

  /**
   * Handles user logout: calls backend API, clears local storage, and navigates to login.
   */
  logout(): void {
    let email = localStorage.getItem(this.emailKey as string);
    if (!email) {
      this.router.navigate(['/login']);
      return
    }
    // Ensure the logout API call completes before clearing local storage and navigating.
    // `finalize` ensures this block runs regardless of success or error of the API call.
    this.logoutAndInvalidateToken(email as string).pipe(
      finalize(() => {
        this.loader.hide();
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.userRoleKey);
        localStorage.removeItem(this.fullnameKey);
        localStorage.removeItem(this.emailKey);
        sessionStorage.clear(); // Clear session storage as well
        this.isLoggedIn$.next(false); // Update login status
        this.router.navigate(['/login']); // Navigate to login page
      })
    ).subscribe({
      next: (response) => {
        // Only show success message if the API call was successful
        if (response && response.response === true) { // Assuming 'response: true' indicates success from API
          this.showMessage("You have been successfully signed out.");
        }
      },
      error: (error) => {
        // Error already handled in `catchError` of `logoutAndInvalidateToken`,
        // and `finalize` ensures cleanup regardless.
        console.error('An error occurred during logout process:', error);
      }
    });
  }

  /**
   * Displays a snackbar message to the user.
   * @param message The message to display.
   */
  showMessage(message: string) {
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 4000,
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  /**
   * Retrieves the authentication token from local storage.
   * @returns The authentication token string or null.
   */
  getAuthToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * (Private/Internal helper) Parses the role from a JWT token.
   * NOTE: This is client-side parsing and should not be used for security decisions.
   * Server-side role validation (`getRolesFromToken`) is more secure.
   * @param token The JWT token string.
   * @returns The first role found in the token payload, or 'USER' as default.
   */
  private parseRoleFromToken(token: string): string {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.roles && Array.isArray(payload.roles)) {
        return payload.roles[0];
      }
      if (payload.role) {
        return payload.role;
      }
      return 'USER';
    } catch {
      return 'USER';
    }
  }

  /**
   * Requests an OTP for forgot password functionality.
   * @param email User's email.
   * @returns An Observable of ApiResponse.
   */
  requestForgotPasswordOtp(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/request-otp`, { email });
  }

  /**
   * Verifies an OTP for forgot password functionality.
   * @param email User's email.
   * @param otp The OTP received.
   * @returns An Observable of ApiResponse.
   */
  verifyForgotPasswordOtp(email: string, otp: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/verify-otp`, { email, otp });
  }

  /**
   * Verifies an OTP for account verification.
   * @param email User's email.
   * @param otp The OTP received.
   * @returns An Observable of ApiResponse.
   */
  verifyAccountVerificationOtp(email: string, otp: string): Observable<ApiResponse> {
    const payload = { email, otp };
    return this.http.post<ApiResponse>(`/api/auth/account-verification/verify-otp`, payload);
  }

  /**
   * Requests an OTP for account verification.
   * @param email User's email.
   * @returns An Observable of ApiResponse.
   */
  requestAccountVerificationOtp(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/account-verification/request-otp`, { email });
  }

  /**
   * Resets the user's password.
   * @param email User's email.
   * @param newPassword The new password.
   * @returns An Observable of ApiResponse.
   */
  resetPassword(email: string, newPassword: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`/api/auth/forgot-password/reset`, { email, newPassword });
  }

  /**
   * Saves user data (token, email, username, full name, roles) to local storage after successful login.
   * @param res The LoginResponse object.
   */
  saveUserData(res: LoginResponse) {
    localStorage.setItem(this.tokenKey, res.token);
    localStorage.setItem(this.emailKey, res.email);
    localStorage.setItem('username', res.username); // Consider making 'username' a constant like other keys
    localStorage.setItem(this.fullnameKey, res.fullName);
    localStorage.setItem(this.userRoleKey, JSON.stringify(res.roles));
  }

  /**
   * Retrieves the full name from local storage.
   * @returns The full name string or null.
   */
  getFullName(): string | null {
    return localStorage.getItem(this.fullnameKey);
  }

  /**
   * Retrieves the email from local storage.
   * @returns The email string or null.
   */
  getEmail(): string | null {
    return localStorage.getItem(this.emailKey);
  }

  /**
   * Fetches user roles from the backend using the provided authentication token.
   * This is the authoritative source for user roles.
   * @param authToken The authentication token.
   * @returns An Observable of an array of role strings.
   */
  getRolesFromToken(authToken: string | null): Observable<string[]> {
    if (!authToken) {
      return of([]); // If no token, return empty roles array
    }

    const headers = { Authorization: `Bearer ${authToken}` };
    return this.http.get<string[]>('/api/auth/get-roles-by-token', { headers }).pipe(
      catchError(err => {
        console.error('Error fetching roles from token:', err);
        return of([]); // Return empty roles array on error
      })
    );
  }

  /**
   * Fetches user roles from the backend based on email.
   * @param email User's email.
   * @returns An Observable of an array of role strings.
   */
  getRolesByEmail(email: string): Observable<string[]> {
    return this.http.get<string[]>(`/api/auth/roles-by-email/${email}`);
  }

  /**
   * Validates the current user session with the backend.
   * @returns An Observable<boolean> indicating if the session is valid.
   */
  validateSession(): Observable<boolean> {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) return of(false);

    const headers = { Authorization: `Bearer ${token}` };
    return this.http.get<boolean>('/api/auth/validate-session', { headers }).pipe(
      catchError(err => {
        console.error('Session validation failed:', err);
        return of(false); // Session is invalid on error
      })
    );
  }

  fetchLoginResponseAfterOtpVerification(email: string): Observable<LoginResponse> {
    return this.http.get<LoginResponse>(`/api/auth/get-login-response-after-otp-verification`,
      {
        headers: { email }
      });
  }
  saveApplicationSettings(payload: ApplicationSettingsPayload): Observable<any> {
    console.log('Sending settings payload:', payload);
    return this.http.post<any>('/api/auth/save-app-settings', payload);
  }
  getLatestSettings(): Observable<any> {
    return this.http.get<any>('/api/auth/get-app-settings');
  }



}
