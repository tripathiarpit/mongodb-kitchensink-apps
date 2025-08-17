import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import {MaterialModule} from '../../../../material.module';
import {CommonModule} from '@angular/common';
import {AuthService} from '../../../../core/services/AuthService';
import {ApiResponse} from '../../../../shared/model/ApiResponse';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-otp-verification',
  templateUrl: './otp-verification-component.html',
  styleUrls: ['./otp-verification-component.css'],
  imports:[MaterialModule, ReactiveFormsModule, CommonModule]
})
export class OtpVerificationComponent implements OnInit, OnDestroy {
  @Input() email!: string; // email to send OTP
  @Output() verified = new EventEmitter<boolean>();
  @Output() resend = new EventEmitter<void>();

  otpForm!: FormGroup;
  countdown: number = 30;
  resendDisabled = true;
  private countdownSub!: Subscription;

  constructor(private fb: FormBuilder,   private authService: AuthService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });

    this.startCountdown();
  }

  ngOnDestroy(): void {
    if (this.countdownSub) {
      this.countdownSub.unsubscribe();
    }
  }

  submitOtp() {
    if (this.otpForm.valid) {
      this.authService.verifyAccountVerificationOtp(this.email, this.otpForm.value.otp).subscribe({
        next: (response: ApiResponse) => {
          if (response.success) {
            this.verified.emit(true);
            this.snackBar.open(response.message, 'Close', { duration: 5000, panelClass: ['snack-success'] });
          } else {
            this.verified.emit(false);
            this.snackBar.open(response.message, 'Close', { duration: 5000, panelClass: ['snack-error'] });
          }
        },
        error: (err) => {
          // Handle HTTP or network errors
          const message = err?.error?.message || 'Something went wrong. Please try again.';
          this.snackBar.open(message, 'Close', { duration: 5000, panelClass: ['snack-error'] });
        }
      });
    }
  }

  resendOtp() {
    this.authService.requestAccountVerificationOtp(this.email).subscribe({
      next: (response: ApiResponse) => {
        if (response.success) {
          this.verified.emit(true);
          this.snackBar.open(response.message, 'Close', { duration: 5000, panelClass: ['snack-success'] });
        } else {
          this.verified.emit(false);
          this.snackBar.open(response.message, 'Close', { duration: 5000, panelClass: ['snack-error'] });
        }
      },
      error: (err) => {
        const message = err?.error?.message || 'Something went wrong. Please try again.';
        this.snackBar.open(message, 'Close', { duration: 5000, panelClass: ['snack-error'] });
      }
    });
    this.resend.emit();
    this.resendDisabled = true;
    this.countdown = 30;
    this.startCountdown();
  }

  private startCountdown() {
    this.countdownSub = interval(1000)
      .pipe(take(this.countdown))
      .subscribe(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          this.resendDisabled = false;
        }
      });
  }
}
