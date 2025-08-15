import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import {UserService} from '../../../../core/services/UserService';
import {User} from '../../../../shared/model/UserModel';
import {MaterialModule} from '../../../../material.module';
import {CommonModule, DatePipe} from '@angular/common';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {MatMenu, MatMenuTrigger} from '@angular/material/menu';
import {SharedStateService} from '../../../../core/services/SharedStateService';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss'],
  imports: [MaterialModule, DatePipe, MatChipSet, MatChip, MatMenuTrigger, MatMenu, CommonModule]
})
export class UserDetailsComponent implements OnInit {
  user!: User;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private changeDetectorRef: ChangeDetectorRef,
    private sharedState: SharedStateService,
  ) {
  }
  close(): void {

  }
  ngOnInit(): void {
    const userId = this.route.snapshot.paramMap.get('id');
    if (userId) {
      this.userService.gerUserByEmailId(userId).subscribe({
        next: (res) => {
          this.user = res;
          this.isLoading = false;
          this.changeDetectorRef.detectChanges();
        },
        error: () => {
          this.snackBar.open('Error fetching user details', 'Close', {duration: 3000});
          this.isLoading = false;
        }
      });
    }
  }

  editUser(user: User) {
    this.router.navigate(['dashboard/edit-profile', user.email]);
  }
  resetPassword(user: User) {
    this.sharedState.setShowSignInLink(false);
    this.router.navigate(['dashboard/forgot-password']);
  }
}
