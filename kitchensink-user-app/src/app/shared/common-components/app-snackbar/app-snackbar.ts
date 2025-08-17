import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-snack-bar-error',
  templateUrl: './app-snackbar.html',
  imports: [
    MatIcon
  ],
  styles: [`
    .snack-bar-content {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #fff;
    }
  `]
})
export class AppSnackbarComponent {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) {}
}
