import { Component, OnInit, Input } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {MatChip, MatChipGrid, MatChipInput, MatChipInputEvent, MatChipRow} from '@angular/material/chips';
import {User, UserPage} from '../../../../shared/model/UserModel';
import {MaterialModule} from '../../../../material.module';
import {CommonModule} from '@angular/common';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {ActivatedRoute, Router} from '@angular/router';
import {UserService} from '../../../../core/services/UserService';
import {AuthService} from '../../../../core/services/AuthService';

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.scss'],
  imports: [MaterialModule, CommonModule, MatChipInput, MatSlideToggle, ReactiveFormsModule, MatChipGrid, MatChipRow]
})
export class EditProfileComponent implements OnInit {
  userData: User | undefined;
  emailId:string = '';
  userForm!: FormGroup;
  roles: string[] = [];
  separatorKeysCodes: number[] = [ENTER, COMMA];
  allowedRoles: string[] = ['USER', 'ADMIN'];
  isEnabledString: string = '';
  constructor(private fb: FormBuilder, private route: ActivatedRoute,
              private userService: UserService, private router: Router,private authService: AuthService,) {}

  ngOnInit(): void {
    this.initializeForm();
    this.emailId = this.route.snapshot.paramMap.get('email')!;
    if(this.authService.getEmail() == this.emailId) {
      this.userService.gerUserByEmailId(this.emailId).subscribe((user: User | undefined) => {
        this.userData = user;
        this.populateForm(this.userData as User);
      });
      this.userForm.get('twoFAEnabled')?.valueChanges.subscribe((enabled: boolean) => {
        this.isEnabledString = enabled ? 'Enabled' : 'Disabled';
        console.log('2FA status changed:', this.isEnabledString);
      });
    }
    else{
      this.router.navigate(['/access-denied']);
    }

  }

  initializeForm(): void {
    this.userForm = this.fb.group({
      username: new FormControl({value: "", disabled: true}),
      email: ['', [Validators.required, Validators.email]],
      roles: [[]],
      active: new FormControl({value: true, disabled: true}),
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
      twoFAEnabled:userData.twoFAEnabled,
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
    this.isEnabledString = userData.twoFAEnabled? 'Enabled':'Disabled';
  }

  // addRole(event: MatChipInputEvent): void {
  //   const value = (event.value || '').trim();
  //   if (value) {
  //     this.roles.push(value);
  //     this.userForm.get('roles')?.setValue([...this.roles]);
  //   }
  //   event.chipInput!.clear();
  // }
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

      // Prepare UserDto object
      const updatedUser: User = {
        id: formValue.id,
        username: formValue.username,
        email: formValue.email,
        roles: this.roles,
        active: formValue.active,
        twoFAEnabled: formValue.twoFAEnabled,
        createdAt: this.userData?.createdAt || new Date().toISOString(),
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

      // Emit or call service to save the user
      console.log('Saving user:', updatedUser);
      // this.userService.updateUser(updatedUser).subscribe(...);
    } else {
      console.log('Form is invalid');
      this.markFormGroupTouched();
    }
  }

  cancel(): void {
    this.userForm.reset();
    this.router.navigate(['dashboard/user-management']);
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
}
