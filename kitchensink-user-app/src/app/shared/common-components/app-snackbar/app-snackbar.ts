import {Component, Inject, OnInit} from '@angular/core';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';
import {MatIcon} from '@angular/material/icon';
import {MaterialModule} from '../../../material.module';

@Component({
  selector: 'app-snack-bar-error',
  templateUrl: './app-snackbar.html',
  styleUrls: ['./app-snackbar.css'],
  imports: [
    MatIcon, MaterialModule
  ]
})
export class AppSnackbarComponent implements OnInit {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any,
              public snackBarRef: MatSnackBarRef<AppSnackbarComponent>) {}
  ngOnInit() {
      console.log('AppSnackbarComponent ngOnInit');
  }
}
