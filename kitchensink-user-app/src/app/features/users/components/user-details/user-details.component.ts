import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router'; // Import ActivatedRoute
import { MatSnackBar } from '@angular/material/snack-bar';
import {UserService} from '../../../../core/services/UserService';
import {User} from '../../../../shared/model/UserModel';
import {MaterialModule} from '../../../../material.module';
import {CommonModule, DatePipe} from '@angular/common';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {MatMenu, MatMenuTrigger} from '@angular/material/menu';
import {SharedStateService} from '../../../../core/services/SharedStateService';
import {ForgotPasswordComponent} from '../../../auth/reset-password/forgot-password-component';
import {LoaderService} from '../../../../core/services/LoaderService';
import {
  ConfirmDialogComponent
} from '../../../../shared/common-components/confirm-dialog.component/confirm-dialog.component';
import {AppSnackbarComponent} from '../../../../shared/common-components/app-snackbar/app-snackbar';
import {AuthService} from '../../../../core/services/AuthService';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss'],
  imports: [MaterialModule, DatePipe, MatChipSet, MatChip, MatMenuTrigger, MatMenu, CommonModule, ForgotPasswordComponent]
})
export class UserDetailsComponent implements OnInit , OnDestroy{
  user!: User;
  isLoading = true;
  protected email: string | null = null;
  protected showPasswordTemplate: boolean = false;

  constructor(
    private route: ActivatedRoute, // Inject ActivatedRoute
    private router: Router,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private changeDetectorRef: ChangeDetectorRef,
    private sharedState: SharedStateService,
    private loaderService: LoaderService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {
    // Remove the logic that relies on navigation state in the constructor.
    // It's not reliable for direct URL access or page refreshes.
    // this.email = nav?.extras?.state?.['email'] ?? history.state['email'] ?? '';
  }

  ngOnDestroy(): void {
    this.sharedState.setShowSignInLink(true);
  }

  ngOnInit(): void {
    this.loaderService.show();

    // Check if an 'id' parameter exists in the URL
    const userIdParam = this.route.snapshot.paramMap.get('id');

    if (userIdParam) {
      // If an ID is present, it means we are viewing a specific user's details
      // You should fetch user by ID here, if your backend supports it.
      // For now, we'll use the email derived from the ID, but ideally, you'd fetch by ID.
      // Or, if your backend uses email as ID in the URL, then use userIdParam as email.
      this.email = userIdParam; // Assuming ID here means email if that's how your route is structured
      this.fetchUserDetails(this.email);

    } else {
      // If no ID parameter is present, it means the logged-in user is viewing their own profile.
      // Get the email directly from AuthService.
      this.email = this.authService.getEmail();
      if (this.email) {
        this.fetchUserDetails(this.email);
      } else {
        this.loaderService.hide();
        this.snackBar.open('User email not found. Please log in again.', 'Close', { duration: 5000 });
        this.isLoading = false;
        // Optionally, redirect to login if email is not found
        this.router.navigate(['/login']);
      }
    }
  }

  private fetchUserDetails(email: string): void {
    this.userService.gerUserByEmailId(email).subscribe({
      next: (res) => {
        this.loaderService.hide();
        this.user = res;
        this.isLoading = false;
        this.changeDetectorRef.detectChanges();
      },
      error: () => {
        this.loaderService.hide();
        this.snackBar.open('Error fetching user details', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  editUser(user: User) {
    // Removed navigateByUrl and skipLocationChange for cleaner navigation
    this.router.navigate(['dashboard/edit-profile'], { state: { email:  user.email} });
  }

  resetPassword(user: User) {
    this.showPasswordTemplate = true;
    this.sharedState.setShowSignInLink(false);
  }

  deleteUser(user: any): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {title: 'Confirm Delete', message: `Are you sure you want to delete ${user.email}?`}
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.userService.deleteUser(user.email).subscribe({
          next: (res) => {
            this.snackBar.open(res.message || 'User deleted successfully', 'Close', {duration: 3000});
            this.authService.logout();
          },
          error: (err) => {
            let errorMessage = err.error.message ;
            this.snackBar.openFromComponent(AppSnackbarComponent, {
              data: { errorMessage },
              duration: 3000,
              verticalPosition: 'top', // position at the top
              panelClass: ['error-snackbar'] // custom CSS
            });
          }
        });
      }
    });
  }
}
