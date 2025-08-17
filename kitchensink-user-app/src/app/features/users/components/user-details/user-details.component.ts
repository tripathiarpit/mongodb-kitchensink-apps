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

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss'],
  imports: [MaterialModule, DatePipe, MatChipSet, MatChip, MatMenuTrigger, MatMenu, CommonModule, ForgotPasswordComponent]
})
export class UserDetailsComponent implements OnInit , OnDestroy{
  user!: User;
  isLoading = true;
  protected email: string;
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
    const nav = this.router.getCurrentNavigation();
    this.email = nav?.extras?.state?.['email'] ?? history.state['email'] ?? '';
  }

  ngOnDestroy(): void {
    this.sharedState.setShowSignInLink(true);
    }

  ngOnInit(): void {
    this.loaderService.show();
    if (this.email) {
      this.userService.gerUserByEmailId(this.email).subscribe({
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
    } else {
      this.snackBar.open('No email provided', 'Close', { duration: 3000 });
    }
  }

  editUser(user: User) {
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate(['dashboard/edit-profile'], { state: { email:  user.email} });
    });
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
