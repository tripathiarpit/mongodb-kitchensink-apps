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
  constructor(private fb: FormBuilder ,
  private router: Router,
              private authService: AuthService){
    this.loginForm = this.fb.group({
      email: ['admin@example.com', [Validators.required, Validators.email]],
      password: ['admin', [Validators.required, Validators.minLength(2)]]
    });
  }

  onSubmit() {
    this.errorMessage = null; // reset before new attempt

    if (this.loginForm.valid) {
      const email = this.loginForm.value.email;
      const password = this.loginForm.value.password;

      if (email && password) {
        this.authService.login(email, password).subscribe({
          next: (res) => {
            console.log('Login successful', res);
            this.router.navigate(['/dashboard']);
          },
          error: (err) => {
            console.error('Login failed', err);
            this.errorMessage = 'Invalid email or password. Please try again.';
          }
        });
      } else {
        this.errorMessage = 'Please enter both email and password.';
      }
    } else {
      this.errorMessage = 'Please fill in the form correctly.';
    }
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
