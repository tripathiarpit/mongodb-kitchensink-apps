import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';
import { MatSliderModule } from '@angular/material/slider';
import {Router} from '@angular/router';
import {AuthService} from '../../core/services/AuthService';
import {LoaderService} from "../../core/services/LoaderService";
import {finalize} from "rxjs/operators";
import {MatSnackBar} from "@angular/material/snack-bar";
import {AppSnackbarComponent} from "../../shared/common-components/app-snackbar/app-snackbar";

@Component({
  selector: 'app-resource-settings',
  templateUrl: './app-resource-settings.component.html',
  styleUrls: ['./app-resource-settings.component.scss'],
  imports: [MaterialModule, CommonModule, MatSliderModule],
  standalone: true // Add this if using standalone components
})
export class AppResourceSettingsComponent implements OnInit {
    settingsForm!: FormGroup;

  constructor(private fb: FormBuilder, private router: Router, private authService: AuthService
  , private loader: LoaderService, private snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.initializeForm();
    this.setupFormValueChanges();
  }

  private initializeForm(): void {
    this.settingsForm = this.fb.group({
      days: [0, [Validators.required, Validators.min(0), Validators.max(30)]],
      sessionExpiryHours: [0, [Validators.required, Validators.min(0), Validators.max(24)]],
      sessionExpiryMinutes: [30, [Validators.required, Validators.min(0), Validators.max(59)]],
      sessionExpirySeconds: [0, [Validators.required, Validators.min(0), Validators.max(59)]],


      forgotOtpExpiryHours: [0, [Validators.required, Validators.min(0), Validators.max(24)]],
      forgotOtpExpiryMinutes: [5, [Validators.required, Validators.min(0), Validators.max(59)]],
      forgotOtpExpirySeconds: [0, [Validators.required, Validators.min(0), Validators.max(59)]],

      registrationOtpExpiryHours: [0, [Validators.required, Validators.min(0), Validators.max(24)]],
      registrationOtpExpiryMinutes: [10, [Validators.required, Validators.min(0), Validators.max(59)]],
      registrationOtpExpirySeconds: [0, [Validators.required, Validators.min(0), Validators.max(59)]]
    });
    this.loadSettings();
  }

  private setupFormValueChanges(): void {
    this.settingsForm.valueChanges.subscribe(values => {
      this.validateMinimumTimes();
    });
  }

  private validateMinimumTimes(): void {
    const sessionTotal = this.calculateTotalSeconds('session');
    const forgotTotal = this.calculateTotalSeconds('forgot');
    const registrationTotal = this.calculateTotalSeconds('registration');

    if (sessionTotal < 300) {
      console.warn('Session time might be too short for practical use');
    }

    if (forgotTotal < 30 || registrationTotal < 30) {
      console.warn('OTP expiry time might be too short');
    }
  }
  private calculateTotalSeconds(type: 'session' | 'forgot' | 'registration'): number {
    const formValue = this.settingsForm.value;

    let hours = 0, minutes = 0, seconds = 0 , days = 0;

    switch (type) {
      case 'session':
        hours = formValue.sessionExpiryHours || 0;
        minutes = formValue.sessionExpiryMinutes || 0;
        seconds = formValue.sessionExpirySeconds || 0;
        days = formValue.days || 0;
        break;
      case 'forgot':
        hours = formValue.forgotOtpExpiryHours || 0;
        minutes = formValue.forgotOtpExpiryMinutes || 0;
        seconds = formValue.forgotOtpExpirySeconds || 0;
        break;
      case 'registration':
        hours = formValue.registrationOtpExpiryHours || 0;
        minutes = formValue.registrationOtpExpiryMinutes || 0;
        seconds = formValue.registrationOtpExpirySeconds || 0;
        break;
    }

    return (days* 86400)+(hours * 3600) + (minutes * 60) + seconds;
  }

  /**
   * Get formatted time display for the UI
   */
  getFormattedTime(type: 'session' | 'forgot' | 'registration'): string {
    const totalSeconds = this.calculateTotalSeconds(type);

    if (totalSeconds === 0) {
      return '0 seconds';
    }

    let remainingSeconds = totalSeconds;

    const days = Math.floor(remainingSeconds / 86400);
    remainingSeconds %= 86400;

    const hours = Math.floor(remainingSeconds / 3600);
    remainingSeconds %= 3600;

    const minutes = Math.floor(remainingSeconds / 60);


    const seconds = remainingSeconds % 60;

    const parts: string[] = [];

    if (days > 0) {
      parts.push(`${days} day${days !== 1 ? 's' : ''}`);
    }
    if (hours > 0) {
      parts.push(`${hours} hour${hours !== 1 ? 's' : ''}`);
    }
    if (minutes > 0) {
      parts.push(`${minutes} minute${minutes !== 1 ? 's' : ''}`);
    }
    if (seconds > 0) {
      parts.push(`${seconds} second${seconds !== 1 ? 's' : ''}`);
    }

    return parts.join(', ');
  }

  saveSettings(): void {

    if (this.settingsForm.valid) {
      const sessionTotalSeconds = this.calculateTotalSeconds('session');
      const forgotOtpTotalSeconds = this.calculateTotalSeconds('forgot');
      const registrationOtpTotalSeconds = this.calculateTotalSeconds('registration');
      if (forgotOtpTotalSeconds < 30) { // Less than 30 seconds
        this.showMessage('Forgot password OTP expiry time should be at least 30 seconds');
        return;
      }

      if (registrationOtpTotalSeconds < 30) { // Less than 30 seconds
        this.showMessage('Registration OTP expiry time should be at least 30 seconds');
        return;
      }
      const payload = {
        sessionExpirySeconds: sessionTotalSeconds,
        forgotPasswordOtpExpirySeconds: forgotOtpTotalSeconds,
        userRegistrationOtpExpirySeconds: registrationOtpTotalSeconds,
      };

      console.log('Formatted display:', {
        session: this.getFormattedTime('session'),
        forgot: this.getFormattedTime('forgot'),
        registration: this.getFormattedTime('registration')
      });
      this.loader.show();
      this.authService.saveApplicationSettings(payload).pipe(
          finalize(() => {
            this.loader.hide();
          })
      ).subscribe({
        next: (response) => {
          console.log('Settings saved successfully!', response);
          this.showMessage('Settings saved successfully!');
        },
        error: (error) => {
          this.loader.hide();
          console.error('Error saving settings:', error);
          // Display an error message
          this.showMessage('Error saving settings. Please try again.');
        }
      });

      this.showSuccessMessage('Settings saved successfully!');

      this.settingsForm.markAsPristine();
      this.settingsForm.markAsUntouched();
    } else {
      console.log('Form is invalid. Please check all fields.');

      this.settingsForm.markAllAsTouched();
      this.showMessage('Please fix the validation errors before saving.');
    }
  }

  /**
   * Load existing settings from the backend and populate the form
   */
  loadSettings(): void {
    Example: this.authService.getLatestSettings().subscribe(settings => {
      const sessionTime = this.convertSecondsToHMS(settings.sessionExpirySeconds);
      const forgotTime = this.convertSecondsToHMS(settings.forgotPasswordOtpExpirySeconds);
      const regTime = this.convertSecondsToHMS(settings.userRegistrationOtpExpirySeconds);

      this.settingsForm.patchValue({
        sessionExpiryHours: sessionTime.hours,
        sessionExpiryMinutes: sessionTime.minutes,
        sessionExpirySeconds: sessionTime.seconds,
        forgotOtpExpiryHours: forgotTime.hours,
        forgotOtpExpiryMinutes: forgotTime.minutes,
        forgotOtpExpirySeconds: forgotTime.seconds,
        registrationOtpExpiryHours: regTime.hours,
        registrationOtpExpiryMinutes: regTime.minutes,
        registrationOtpExpirySeconds: regTime.seconds,
      });
    });
  }

  /**
   * Helper function to convert total seconds back into Hours, Minutes, Seconds for loading
   */
  private convertSecondsToHMS(totalSeconds: number): { hours: number, minutes: number, seconds: number } {
    const hours = Math.floor(totalSeconds / 3600);
    const remainingSecondsAfterHours = totalSeconds % 3600;
    const minutes = Math.floor(remainingSecondsAfterHours / 60);
    const seconds = remainingSecondsAfterHours % 60;

    return { hours, minutes, seconds };
  }

  /**
   * Reset form to default values
   */
  resetToDefaults(): void {
    this.settingsForm.patchValue({
      sessionExpiryHours: 0,
      sessionExpiryMinutes: 30,
      sessionExpirySeconds: 0,
      forgotOtpExpiryHours: 0,
      forgotOtpExpiryMinutes: 5,
      forgotOtpExpirySeconds: 0,
      registrationOtpExpiryHours: 0,
      registrationOtpExpiryMinutes: 10,
      registrationOtpExpirySeconds: 0,
    });
  }
  private showSuccessMessage(message: string): void {
    this.snackBar.open(message, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
    console.log('Success:', message);
  }
  showMessage(message: string) {
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 3000,
      verticalPosition: 'top', // position at the top
      panelClass: ['error-snackbar'] // custom CSS
    });
  }

}
