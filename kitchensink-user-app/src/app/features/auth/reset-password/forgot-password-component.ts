import {Component, EmbeddedViewRef, Input, OnDestroy, OnInit, TemplateRef, ViewChild} from '@angular/core';
import { MaterialModule } from '../../../material.module';
import {
  FormBuilder,
  FormGroup,
  FormControl,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { AuthService } from '../../../core/services/AuthService';
import {MatSnackBar, MatSnackBarRef} from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import {AsyncPipe, NgIf} from '@angular/common';
import { Router } from '@angular/router';
import {MatStepper} from '@angular/material/stepper';
import {interval, Subscription, take} from 'rxjs';
import {SharedStateService} from '../../../core/services/SharedStateService';
import {LoaderService} from '../../../core/services/LoaderService';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forget-password.component.scss'],
  standalone: true,
  imports: [MaterialModule, ReactiveFormsModule, NgIf, AsyncPipe]
})
export class ForgotPasswordComponent implements OnInit, OnDestroy {
  emailForm!: FormGroup<{ email: FormControl<string> }>;
  otpForm!: FormGroup<{ otp: FormControl<string> }>;
  passwordForm!: FormGroup<{
    newPassword: FormControl<string>;
    confirmPassword: FormControl<string>;
  }>;
  @ViewChild('customSnack') customSnack!: TemplateRef<any>;
  @ViewChild('stepper') stepper!: MatStepper;
  hidePassword = true;
  hideConfirmPassword = true;
  @Input('changingOwnPassword') changingOwnPassword!: boolean;
  @Input('currentUserEmail') email:string ='';

  otpSending = false;
  otpSent = false;
  otpVerifying = false;
  otpVerified = false;
  resendDisabled = true;
  countdown: number = 30;
  private countdownSub!: Subscription;
  private matref!: MatSnackBarRef<EmbeddedViewRef<any>>;
  showSIgnInLink: boolean = true;
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snack: MatSnackBar,
    private router: Router,
    public sharedState: SharedStateService,
    private loaderService: LoaderService
  ) {
  }

  ngOnInit(): void {
    if (this.changingOwnPassword) {
      this.authService.validateSession().subscribe(isValid => {
        if (isValid) {
          this.emailForm.get('email')?.disable();
        } else {
          this.emailForm.get('email')?.enable();
        }
      });
    }
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    this.emailForm = this.fb.nonNullable.group({
      email: [{ value: this.email, disabled: this.changingOwnPassword },[Validators.required, Validators.email, Validators.pattern(emailRegex)]],
    });
    this.otpForm = this.fb.nonNullable.group({
      otp: [
        '',
        [
          Validators.required,
          Validators.minLength(6),
          Validators.maxLength(6),
          Validators.pattern(/^\d{6}$/)
        ],
      ],
    });

    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', [Validators.required]]
    }, {validators: this.passwordMatchValidator});
    this.sharedState.showSignInLink$.subscribe(value => {
      this.showSIgnInLink = value;
    });
  }

  private showError(err: string, fallback = 'Something went wrong'): void {
    const msg = err ? err : fallback;
    this.snack.openFromTemplate(this.customSnack, {
      data: {message: msg},
      duration: 40000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['custom-snack-panel'],
    });
  }
  getEmailErrorMessage(fieldName: string): string {
    const field = this.emailForm.get(fieldName);
    if (fieldName === 'email' && field?.hasError('email') || field?.hasError('pattern')) {
      return 'Please enter a valid email address';
    }
    return '';
  }

  private showSuccessSnack(err: string, fallback = 'Action Success', duration: number): void {
    const msg = err ? err : fallback;
    this.matref = this.snack.openFromTemplate(this.customSnack, {
      data: {message: msg},
      duration: duration,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['custom-snack-panel'],
    });
  }

  sendOtp(): void {
    const {email} = this.emailForm.getRawValue();
    if (!email) {
      this.snack.open('Please enter a valid email', 'Close', {duration: 3000});
      return;
    }

    this.otpSending = true;
    this.startCountdown();
    this.otpSent = false;
    this.authService.requestForgotPasswordOtp(email).subscribe({
      next: (response) => {
        if (response.success) {
          this.showSuccessSnack(response.message, "", 8000);
          this.otpSent = true;
          this.stepper.next();
          this.otpForm.reset();
        } else {
          this.showError(response.message || 'Failed to send OTP');
          this.otpSent = false;
        }
      },
      error: (err) => {
        this.showError(err.error.message, 'Failed to send OTP');
        this.otpSent = false;
      },
      complete: () => {
        this.otpSending = false;
      }
    });
  }

  reSendOtp(): void {
    const {email} = this.emailForm.getRawValue();
    if (!email) {
      this.snack.open('Please enter a valid email', 'Close', {duration: 3000});
      return;
    }

    this.otpSending = true;
    this.startCountdown();
    this.otpSent = false;
    this.authService.requestForgotPasswordOtp(email).subscribe({
      next: (response) => {
        if (response.success) {
          this.showSuccessSnack(response.message, "", 8000);
          this.otpSent = true;
          this.otpForm.reset();
        } else {
          this.showError(response.message || 'Failed to send OTP');
          this.otpSent = false;
        }
      },
      error: (err) => {
        this.showError(err.error.message, 'Failed to send OTP');
        this.otpSent = false;
      },
      complete: () => {
        this.otpSending = false;
      }
    });
  }



  verifyOtp(): void {
    const {email} = this.emailForm.getRawValue();
    const {otp} = this.otpForm.getRawValue();
    if (!email || !otp) {
      this.snack.open('Enter email and OTP', 'Close', {duration: 3000});
      return;
    }

    this.otpVerifying = true;
    this.otpVerified = false;
    this.authService.verifyForgotPasswordOtp(email, otp).subscribe({
      next: (res: { success: boolean; code: any; message: string }) => {
        if (res.success) {
          this.otpVerified = true;
          this.showSuccessSnack(res.message, "", 8000);
          this.stepper.next();
        } else {
          this.otpVerified = false;
          this.showError(res.message || 'OTP verification failed', 'OTP verification');
        }
      },
      error: (err) => {
        this.otpVerified = false;
        this.showError(err.error?.message || 'Something went wrong', 'OTP verification failed');
      },
      complete: () => this.otpVerifying = false
    });
  }

  resetPassword(): void {
    const {email} = this.emailForm.getRawValue();
    const {newPassword, confirmPassword} = this.passwordForm.getRawValue();

    if (!email || !newPassword || !confirmPassword) {
      this.snack.open('Fill all fields', 'Close', {duration: 3000});
      return;
    }

    if (newPassword !== confirmPassword) {
      this.snack.open('Passwords do not match', 'Close', {duration: 3000});
      return;
    }
    this.loaderService.show();
    this.authService.resetPassword(email, newPassword).subscribe({
      next: (res: { success: boolean; code: any; message: string }) => {
        this.emailForm.reset();
        this.otpForm.reset();
        this.passwordForm.reset();
        this.otpSent = false;
        this.otpVerified = false;
        this.populateSnackbarForRedirect(res.message);
        this.loaderService.hide();
        this.passwordForm.disable();
      },
      error: (err) => {
        this.showError(err, 'Password reset failed')
        this.loaderService.hide();
      },
    });
  }

  populateSnackbarForRedirect(message: string): void {
    this.showSuccessSnack(message, "", 8000);
    const countdownSeconds = 2;
    let remaining = countdownSeconds;

    interval(1000)
      .pipe(take(countdownSeconds))
      .subscribe(() => {
        remaining--;
        this.matref.containerInstance._label.nativeElement.innerText =
          `Password reset successfully. Redirecting in ${remaining} seconds...`;
      });

    setTimeout(() => {
      this.onBackToLogin();
    }, countdownSeconds * 1000);
  }

  onBackToLogin(): void {
    if(this.authService.isLoggedIn()) {
      this.router.navigate([this.authService.getDashboardUrl()]);
    } else {
      this.router.navigate(['/login']);
    }
  }


  startCountdown() {
    this.resendDisabled = true;
    this.countdown = 30;

    this.countdownSub?.unsubscribe();

    this.countdownSub = interval(1000)
      .pipe(take(30))
      .subscribe(() => {
        this.countdown--;
        if (this.countdown === 0) {
          this.resendDisabled = false;
        }
      });
  }

  passwordValidator(control: any) {
    const value = control.value;
    if (!value) return null;
    const hasUpperCase = /[A-Z]+/.test(value);
    const hasLowerCase = /[a-z]+/.test(value);
    const hasNumeric = /[0-9]+/.test(value);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]+/.test(value);

    const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecial;
    return valid ? null : { invalidPassword: true };
  }

  passwordMatchValidator(group: FormGroup) {
    const passwordControl = group.get('newPassword');
    const confirmPasswordControl = group.get('confirmPassword');

    if (passwordControl?.value !== confirmPasswordControl?.value) {
      confirmPasswordControl?.setErrors({ ...confirmPasswordControl.errors, passwordMismatch: true });
    } else {
      if (confirmPasswordControl?.hasError('passwordMismatch')) {
        const errors = { ...confirmPasswordControl.errors };
        delete errors['passwordMismatch'];
        confirmPasswordControl.setErrors(Object.keys(errors).length ? errors : null);
      }
    }

    return null;
  }

  getPersonalInfoErrorMessage(fieldName: string): string {
    const field = this.passwordForm.get(fieldName);
    if (field?.hasError('required')) return `${fieldName} is required`;
    if (field?.hasError('minlength')) return `${fieldName} is too short`;
    if (field?.hasError('pattern')) return `${fieldName} contains invalid characters`;
    if (field?.hasError('passwordMismatch')) return 'Passwords do not match';
    if (field?.hasError('invalidPassword')) return 'Password must contain uppercase, lowercase, number and special character';
    return '';
  }

  ngOnDestroy() {
    this.countdownSub?.unsubscribe();
    this.snack.ngOnDestroy();
  }
}
