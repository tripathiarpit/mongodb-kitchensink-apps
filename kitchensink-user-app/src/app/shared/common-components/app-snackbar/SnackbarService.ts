import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import {AppSnackbarComponent} from './app-snackbar';

@Injectable({
  providedIn: 'root'
})
export class SnackbarService {
  constructor(private snackBar: MatSnackBar) {}

  showMessage(message: string, type: 'success' | 'error' = 'error', showCloseButton: boolean = true) {
    let panelClass = (type === 'success') ? ['success-snackbar'] : ['error-snackbar'];
    this.snackBar.open(message, 'Close', { duration: 5000, panelClass: panelClass , verticalPosition: 'top'});
  }
}
