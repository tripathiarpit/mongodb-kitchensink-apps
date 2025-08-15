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
import {AuthService} from '../../core/services/AuthService';
import {LoaderService} from '../../core/services/LoaderService';

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
    imports:[CommonModule, MatIconModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, MaterialModule]
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = true;
  errorMessage: string | null = null;
  isLoading = false;
  constructor(private fb: FormBuilder ,
  private router: Router,
              private authService: AuthService, private loaderService: LoaderService){
    this.loginForm = this.fb.group({
      email: ['admin@example.com', [Validators.required, Validators.email]],
      password: ['admin', [Validators.required, Validators.minLength(2)]]
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

    const { email, password } = this.loginForm.value;
    this.loaderService.show();
    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log('Login successful', response);
        this.isLoading = false;
        this.authService.saveUserData(response);
        // Navigation will happen automatically after successful login
        // because AuthService updates the authentication state
        setTimeout(() => {
          this.loaderService.hide();
          this.router.navigate(['/dashboard']);
        }, 1000);
      },
      error: (error) => {
        console.error('Login failed', error);
        this.isLoading = false;
        this.errorMessage = error?.message || 'Login failed. Please try again.';
        this.loaderService.hide();
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
    return this.loginForm.get('email')?.hasError('email') ? 'Not a valid email' : '';
  }

  getPasswordErrorMessage() {
    if (this.loginForm.get('password')?.hasError('required')) {
      return 'Password is required';
    }
    return this.loginForm.get('password')?.hasError('minlength') ? 'Password must be at least 6 characters' : '';
  }

}
