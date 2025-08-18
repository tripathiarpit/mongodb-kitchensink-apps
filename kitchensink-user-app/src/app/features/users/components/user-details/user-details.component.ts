import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
import {finalize} from 'rxjs/operators';

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
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private changeDetectorRef: ChangeDetectorRef,
    private sharedState: SharedStateService,
    private loaderService: LoaderService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {
  }

  ngOnDestroy(): void {
    this.loaderService.hide();
    this.sharedState.setShowSignInLink(true);
  }

  ngOnInit(): void {
    this.loaderService.show();

    const userIdParam = this.route.snapshot.paramMap.get('id');

    if (userIdParam) {
      this.email = userIdParam;
      this.fetchUserDetails(this.email);

    } else {
      this.email = this.authService.getEmail();
      if (this.email) {
        this.fetchUserDetails(this.email);
      } else {
        this.loaderService.hide();
        this.snackBar.open('User email not found. Please log in again.', 'Close', { duration: 5000 });
        this.isLoading = false;
        this.router.navigate(['/login']);
      }
    }
  }

  private fetchUserDetails(email: string): void {
    this.userService.gerUserByEmailId(email).pipe(
      finalize(() => {
        // This will be called on both success and error
        this.loaderService.hide();
        this.isLoading = false; // It's good practice to set isLoading here as well
      })
    ).subscribe({
      next: (res) => {
        this.user = res;
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.snackBar.open('Error fetching user details', 'Close', { duration: 3000 });
        // The loader and isLoading state are handled in finalize, so no need to repeat here
      }
    });
  }

  editUser(user: User) {
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
              verticalPosition: 'top',
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    });
  }
}
