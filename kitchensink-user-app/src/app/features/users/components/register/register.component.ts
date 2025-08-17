import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { StepperOrientation } from '@angular/cdk/stepper';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BreakpointObserver } from '@angular/cdk/layout';
import {MaterialModule} from '../../../../material.module';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {RegistrationRequest} from '../../../../shared/model/UserRegistrationModel';
import {UserService} from '../../../../core/services/UserService';
import {LoaderService} from '../../../../core/services/LoaderService';

export interface Address {
  street: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
}

export interface SignupData {
  email: string;
  phoneNumber: string;
  address: Address;
}

@Component({
  selector: 'app-signup',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  imports: [MaterialModule, ReactiveFormsModule, CommonModule]
})
export class SignupComponent implements OnInit {
  personalInfoForm!: FormGroup;
  contactForm!: FormGroup;
  addressForm!: FormGroup;
  registrationSuccess = false;
  successMessage:string| undefined = '';
  errorMessage:string | undefined= '';
  stepperOrientation: Observable<StepperOrientation>;
  hidePassword = true;
  hideConfirmPassword = true;
  isLinear = true;

  // Country options - you can expand this list
  countries = [
    'India',
    'United States',
    'United Kingdom',
    'Canada',
    'Australia',
    'Germany',
    'France',
    'Japan',
    'Singapore'
  ];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private userService: UserService,
    private loader: LoaderService
  ) {
    this.stepperOrientation = breakpointObserver
      .observe('(min-width: 800px)')
      .pipe(map(({matches}) => (matches ? 'horizontal' : 'vertical')));
  }

  ngOnInit(): void {
    this.initializeForms();
  }

  initializeForms(): void {
    // Personal Information Form
    this.personalInfoForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2),Validators.pattern(/^[A-Za-z]+$/)]],
      lastName: ['', [Validators.required, Validators.minLength(2),Validators.pattern(/^[A-Za-z]+$/)]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    // Contact Information Form
    this.contactForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern('^[+]?[0-9]{10,15}$')]]
    });

    // Address Information Form
    this.addressForm = this.fb.group({
      street: ['', [Validators.required, Validators.minLength(5)]],
      city: ['', [Validators.required, Validators.minLength(2)]],
      state: ['', [Validators.required, Validators.minLength(2)]],
      pincode: ['', [Validators.required, Validators.pattern(/^\d{5}(-\d{4})?$/)]],
      country: ['', [Validators.required]]
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
    const passwordControl = group.get('password');
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
  // Toggle password visibility
  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.hideConfirmPassword = !this.hideConfirmPassword;
  }

  // Error message getters
  getPersonalInfoErrorMessage(fieldName: string): string {
    const field = this.personalInfoForm.get(fieldName);
    if (field?.hasError('required')) return `${fieldName} is required`;
    if (field?.hasError('minlength')) return `${fieldName} is too short`;
    if (field?.hasError('pattern')) return `${fieldName} contains invalid characters`;
    if (field?.hasError('passwordMismatch')) return 'Passwords do not match';
    if (field?.hasError('invalidPassword')) return 'Password must contain uppercase, lowercase, number and special character';
    return '';
  }

  getContactErrorMessage(fieldName: string): string {
    const field = this.contactForm.get(fieldName);
    if (field?.hasError('required')) return `${fieldName} is required`;
    if (field?.hasError('email')) return 'Please enter a valid email';
    if (field?.hasError('pattern')) return 'Please enter a valid phone number';
    return '';
  }

  getAddressErrorMessage(fieldName: string): string {
    const field = this.addressForm.get(fieldName);
    if (field?.hasError('required')) return `${fieldName} is required`;
    if (field?.hasError('minlength')) return `${fieldName} is too short`;
    if (field?.hasError('pattern')) return 'Please enter a valid pincode';
    return '';
  }

  getPasswordMatchError(): string {
    return this.personalInfoForm.hasError('passwordMismatch') ? 'Passwords do not match' : '';
  }

  onSubmit(): void {
    if (this.personalInfoForm.valid && this.contactForm.valid && this.addressForm.valid) {
      this.errorMessage = undefined;
      this.successMessage = undefined;
      const request: RegistrationRequest = {
        firstName:this.personalInfoForm.value.firstName,
        lastName:this.personalInfoForm.value.lastName,
        email: this.contactForm.value.email,
        password: this.personalInfoForm.value.password,
        phoneNumber: this.contactForm.value.phoneNumber,
        address: {
          street: this.addressForm.value.street,
          city: this.addressForm.value.city,
          state: this.addressForm.value.state,
          pincode: this.addressForm.value.pincode,
          country: this.addressForm.value.country
        },
        city: this.addressForm.value.city,
        pincode: this.addressForm.value.pincode,
        roles: ['USER'] // default role for signup
      };
      this.loader.show();

      this.userService.registerUser(request).subscribe({
        next: res => {
          if (res.success) {
            this.registrationSuccess = true;
            this.successMessage = res.message;
            this.loader.hide();
            // Redirect after 3 seconds
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 3000);
          } else {
            this.registrationSuccess = false;
            this.errorMessage = res.message;
            this.loader.hide();
          }
        },
        error: err => {
          this.loader.hide();
        }
      });

    } else {
      console.log('Form is invalid');
      this.markFormGroupTouched(this.personalInfoForm);
      this.markFormGroupTouched(this.contactForm);
      this.markFormGroupTouched(this.addressForm);
    }
  }


  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }


  onSignIn(): void {
    this.router.navigate(['/login']);

  }

  onBack(): void {
    console.log('Navigate back');

  }
}
