import { Component, OnInit, Input } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {MatChipGrid, MatChipInput, MatChipInputEvent, MatChipRow} from '@angular/material/chips';
import {User, UserPage} from '../../../../shared/model/UserModel';
import {MaterialModule} from '../../../../material.module';
import {CommonModule} from '@angular/common';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {ActivatedRoute, Router} from '@angular/router';
import {UserService} from '../../../../core/services/UserService';
import {AuthService} from '../../../../core/services/AuthService';
import {LoaderService} from '../../../../core/services/LoaderService';
import {AppSnackbarComponent} from '../../../../shared/common-components/app-snackbar/app-snackbar';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.scss'],
  imports: [MaterialModule, CommonModule, MatChipInput, MatSlideToggle, ReactiveFormsModule, MatChipGrid, MatChipRow]
})
export class EditUserComponent implements OnInit {
  userData: User | undefined;
  emailId:string = '';
  userForm!: FormGroup;
  roles: string[] = [];
  separatorKeysCodes: number[] = [ENTER, COMMA];
  allowedRoles: string[] = ['USER', 'ADMIN'];
  constructor(private fb: FormBuilder, private route: ActivatedRoute,
              private userService: UserService, private router: Router,private authService: AuthService,   private snackBar: MatSnackBar,private loaderService: LoaderService) {
    const nav = this.router.getCurrentNavigation();
    this.emailId = nav?.extras.state?.['email'] ?? '';
  }

  ngOnInit(): void {
    this.initializeForm();
    const roleString = this.authService.getUserRole();
    let roles = undefined
    if (roleString != null) {
      roles = JSON.parse(roleString);
    }
    if(this.authService.getEmail() == this.emailId || roles?.includes("ADMIN")) {
      this.userService.gerUserByEmailId(this.emailId).subscribe((user: User | undefined) => {
        this.userData = user;
        this.populateForm(this.userData as User);
      });
    }
    else{
      this.router.navigate(['/access-denied']);
    }

  }

  initializeForm(): void {
    this.userForm = this.fb.group({
      id: [''],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: [{ value: "", disabled: true },[Validators.required, Validators.email]],
      roles: [[]],
      active: [true],
      twoFAEnabled:[false],
      createdAt: [''],
      profile: this.fb.group({
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        phoneNumber: ['', [Validators.pattern(/^\+?[\d\s-()]+$/)]],
        street: [''],
        city: [''],
        state: [''],
        country: [''],
        pincode: ['', [Validators.pattern(/^\d{5,6}$/)]]
      })
    });
  }

  populateForm(userData: User): void {
    this.roles = [...userData.roles];

    this.userForm.patchValue({
      id: userData.id,
      username: userData.username,
      email: userData.email,
      roles: userData.roles,
      active: userData.active,
      createdAt: this.formatDate(userData.createdAt),
      profile: {
        firstName: userData.profile?.firstName || '',
        lastName: userData.profile?.lastName || '',
        phoneNumber: userData.profile?.phoneNumber || '',
        street: userData.profile?.street || '',
        city: userData.profile?.city || '',
        state: userData.profile?.state || '',
        country: userData.profile?.country || '',
        pincode: userData.profile?.pincode || ''
      }
    });
  }
  addRole(event: MatChipInputEvent) {
    const input = event.input;
    const value = event.value.trim().toUpperCase();

    // Only add if value is allowed and not already in the form
    if ((value === 'USER' || value === 'ADMIN') && !this.userForm.value.roles.includes(value)) {
      const roles = [...this.userForm.value.roles, value];
      this.userForm.get('roles')?.setValue(roles);
    }
    if (input) {
      input.value = '';
    }
  }
  removeRole(role: string) {
    const roles = this.userForm.value.roles.filter((r: string) => r !== role);
    this.userForm.get('roles')?.setValue(roles);
  }

  formatDate(instant: string): string {
    try {
      return new Date(instant).toLocaleString();
    } catch {
      return instant;
    }
  }

  save(): void {
    if (this.userForm.valid) {
      const formValue = this.userForm.value;
      const updatedUser = {
        id: formValue.id,
        username: formValue.username,
        roles: this.roles,
        active: formValue.active,
        twoFAEnabled:formValue.twoFAEnabled,
        profile: {
          firstName: formValue.profile.firstName,
          lastName: formValue.profile.lastName,
          phoneNumber: formValue.profile.phoneNumber,
          street: formValue.profile.street,
          city: formValue.profile.city,
          state: formValue.profile.state,
          country: formValue.profile.country,
          pincode: formValue.profile.pincode
        }
      };

      this.loaderService.show();

      this.userService.updateUser(this.emailId, updatedUser).subscribe({
        next: (data) => {
          this.loaderService.hide();
          this.showMessage("User updated successfully");
        },
        error: (err) => {
          this.loaderService.hide();
          this.showMessage("Failed to update user");
        }
      });

    } else {
      this.markFormGroupTouched();
    }
  }

  cancel(): void {
    this.userForm.reset();
    this.router.navigate(['dashboard']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.userForm.controls).forEach(key => {
      const control = this.userForm.get(key);
      control?.markAsTouched();

      if (control?.hasOwnProperty('controls')) {
        Object.keys((control as FormGroup).controls).forEach(nestedKey => {
          (control as FormGroup).get(nestedKey)?.markAsTouched();
        });
      }
    });
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
