// login.component.ts
import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {CommonModule} from '@angular/common';
import {animate, style, transition, trigger} from '@angular/animations';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {Router} from '@angular/router';
import {AuthService, LoginResponse} from '../../core/services/AuthService';
import {LoaderService} from '../../core/services/LoaderService';
import {OtpVerificationComponent} from './otp-verification/otp-verification-component/otp-verification-component';
import {UserService} from '../../core/services/UserService';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AppSnackbarComponent} from '../../shared/common-components/app-snackbar/app-snackbar';
import {SnackbarService} from '../../shared/common-components/app-snackbar/SnackbarService';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: true,
  animations: [
  trigger('fadeIn', [
    transition(':enter', [
      style({ opacity: 0, transform: 'translateY(20px)' }),
      animate('300ms ease-out', style({ opacity: 1, transform: 'none' }))
    ])
  ])
],
    imports:[CommonModule, MatIconModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, MaterialModule, OtpVerificationComponent]
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = true;
  errorMessage: string | null = null;
  isLoading = false;
  isAccountVerificationPending: boolean = false;
  isFirstLogin: boolean = false;
  showOtpVerify: boolean = false;
  currentLoggedInUserEmail: string = '';

  constructor(private fb: FormBuilder,
              private router: Router,
              private authService: AuthService, private loaderService: LoaderService, private userService: UserService, private snackbarService: SnackbarService,) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email, Validators.pattern(emailRegex)]],
      password: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  onSubmit(): void {
    this.errorMessage = null;
    this.isLoading = true;

    if (!this.loginForm.valid) {
      this.errorMessage = 'Please fill in the form correctly.';
      this.isLoading = false;
      return;
    }

    const {email, password} = this.loginForm.value;
    this.loaderService.show();
    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log('Login successful', response);
        this.isLoading = false;
        this.currentLoggedInUserEmail = response.email;
        this.isAccountVerificationPending = response.accountVerificationPending;
        this.isFirstLogin = response.firstLogin;

        if (this.isAccountVerificationPending) {
          this.isLoading = false;
          this.showOtpVerify = true;
          this.loginForm.controls['email'].disable();
          this.loginForm.controls['password'].disable();
          this.loaderService.hide();
        } else {
          // Save user data and navigate immediately.
          this.authService.saveUserData(response);
          this.loaderService.hide();
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error: any) => {
        console.error('Login failed', error);
        this.isLoading = false;
        this.loaderService.hide();
        if (error.status === 400 && error.error) {
          let validationError = '';
          if (error.error.email) {
            validationError = error.error.email;
          } else if (error.error.password) {
            validationError = error.error.password;
          } else {
            validationError = error.error.message || 'Validation failed. Please check your form.';
          }
          this.errorMessage = validationError;

        } else if (error.status === 401) {
          this.errorMessage = error.error.message || 'Invalid email or password.';
        } else {
          this.showMessage(error);
        }
      }
    });
  }


  togglePasswordVisibility() {
    this.hidePassword = !this.hidePassword;
  }

  onSignUp() {
    this.router.navigate(['/signup']);
  }

  onForgotPassword() {
    this.router.navigate(['/forgot-password']);
  }

  getEmailErrorMessage() {
    if (this.loginForm.get('email')?.hasError('required')) {
      return 'Email is required';
    }
    if (this.loginForm.get('email')) {
      return 'Please enter a valid email address';
    }
    return this.loginForm.get('email')?.hasError('email') ? 'Not a valid email' : '';
  }

  getPasswordErrorMessage() {
    if (this.loginForm.get('password')?.hasError('required')) {
      return 'Password is required';
    }
    return this.loginForm.get('password')?.hasError('minlength') ? 'Password must be at least 6 characters' : '';
  }

  isOtpVerificationSuccessful(isOtpSuccessfullyVerified: boolean) {
    this.isAccountVerificationPending = isOtpSuccessfullyVerified;
    this.authService.fetchLoginResponseAfterOtpVerification(this.currentLoggedInUserEmail).subscribe(response => {
      this.loaderService.show();
      this.authService.saveUserData(response);
      this.navigateToDashboard();
    })

  }

  navigateToDashboard(): void {
    setTimeout(() => {

      this.router.navigate(['/dashboard']);
    }, 1000);
    this.loaderService.hide();
  }

  showMessage(message: string) {
    this.snackbarService.showMessage(message,'error',false);
  }
}
