import { Component } from '@angular/core';
import { Router } from '@angular/router';
import {MaterialModule} from '../../../material.module';

@Component({
  selector: 'app-access-denied',
  template: `
    <div class="access-denied-container">
      <mat-card class="access-denied-card">
        <mat-card-header>
          <div mat-card-avatar class="error-avatar">
            <mat-icon>block</mat-icon>
          </div>
          <mat-card-title>Access Denied</mat-card-title>
          <mat-card-subtitle>You don't have permission to access this resource</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div class="error-content">
            <mat-icon class="large-icon" color="warn">warning</mat-icon>
            <p>You need admin privileges to access this page.</p>
            <p>Please contact your administrator if you believe this is an error.</p>
          </div>
        </mat-card-content>

        <mat-card-actions align="end">
          <button mat-raised-button color="primary" (click)="goHome()">Go to Dashboard</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .access-denied-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 20px;
    }

    .access-denied-card {
      max-width: 500px;
      width: 100%;
    }

    .error-avatar {
      background-color: #f44336;
      color: white;
    }

    .error-content {
      text-align: center;
      padding: 20px 0;
    }

    .large-icon {
      font-size: 64px;
      height: 64px;
      width: 64px;
      margin-bottom: 20px;
    }

    .error-content p {
      margin-bottom: 10px;
      color: #666;
    }
  `],
  imports:[MaterialModule]
})
export class AccessDeniedComponent {

  constructor(private router: Router) {}

  goBack() {
    window.history.back();
  }

  goHome() {
    this.router.navigate(['/dashboard']); // Adjust to your home route
  }
}
