import { Component, OnInit } from '@angular/core';
import {CommonModule, DecimalPipe} from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {MatProgressSpinner, MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import {MaterialModule} from '../../material.module';

interface User {
  id: string;
  email: string;
  username: string;
  roles: string[];
  active: boolean;
  createdAt: string;
  isAccountVerificationPending: boolean;
  isFirstLogin: boolean;
}

interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  pendingVerifications: number;
  firstTimeLogins: number;
  newUsersThisMonth: number;
  adminUsers: number;
  regularUsers: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    MaterialModule,
    DecimalPipe,
    MatProgressSpinner
  ],
  template: `
    <div class="container-fluid p-4">
      <div class="row mb-4">
        <div class="col-12">
          <h2 class="text-primary mb-0">
            <mat-icon class="me-2">dashboard</mat-icon>
            Admin Dashboard
          </h2>
          <p class="text-muted">User statistics and system overview</p>
        </div>
      </div>

      <div class="row g-3" *ngIf="!loading; else loadingTemplate">
        <!-- Total Users -->
        <div class="col-lg-3 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-primary">
                <mat-icon>people</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h3 class="mb-0">{{ stats.totalUsers | number }}</h3>
                <p class="text-muted mb-0">Total Users</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Active Users -->
        <div class="col-lg-3 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-success">
                <mat-icon>verified_user</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h3 class="mb-0">{{ stats.activeUsers | number }}</h3>
                <p class="text-muted mb-0">Active Users</p>
                <small class="text-success">{{ getPercentage(stats.activeUsers, stats.totalUsers) }}%</small>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Pending Verifications -->
        <div class="col-lg-3 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-warning">
                <mat-icon>hourglass_empty</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h3 class="mb-0">{{ stats.pendingVerifications | number }}</h3>
                <p class="text-muted mb-0">Pending Verifications</p>
                <small class="text-warning">{{ getPercentage(stats.pendingVerifications, stats.totalUsers) }}%</small>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- New Users This Month -->
        <div class="col-lg-3 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-info">
                <mat-icon>person_add</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h3 class="mb-0">{{ stats.newUsersThisMonth | number }}</h3>
                <p class="text-muted mb-0">New This Month</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>

      <!-- Secondary Stats Row -->
      <div class="row g-3 mt-2" *ngIf="!loading">
        <!-- First Time Logins -->
        <div class="col-lg-4 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-secondary">
                <mat-icon>login</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h4 class="mb-0">{{ stats.firstTimeLogins | number }}</h4>
                <p class="text-muted mb-0">First Time Logins</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Admin Users -->
        <div class="col-lg-4 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-danger">
                <mat-icon>admin_panel_settings</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h4 class="mb-0">{{ stats.adminUsers | number }}</h4>
                <p class="text-muted mb-0">Admin Users</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Regular Users -->
        <div class="col-lg-4 col-md-6">
          <mat-card class="stat-card h-100">
            <mat-card-content class="d-flex align-items-center">
              <div class="stat-icon bg-dark">
                <mat-icon>person</mat-icon>
              </div>
              <div class="stat-info ms-3">
                <h4 class="mb-0">{{ stats.regularUsers | number }}</h4>
                <p class="text-muted mb-0">Regular Users</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="row mt-4" *ngIf="!loading">
        <div class="col-12">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon class="me-2">flash_on</mat-icon>
                Quick Actions
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="d-flex flex-wrap gap-2">
                <button class="btn btn-outline-primary btn-sm" (click)="refreshStats()">
                  <mat-icon class="me-1" style="font-size: 16px;">refresh</mat-icon>
                  Refresh Stats
                </button>
                <button class="btn btn-outline-success btn-sm">
                  <mat-icon class="me-1" style="font-size: 16px;">download</mat-icon>
                  Export Users
                </button>
                <button class="btn btn-outline-warning btn-sm">
                  <mat-icon class="me-1" style="font-size: 16px;">notifications</mat-icon>
                  Send Verification Reminders
                </button>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>

    <!-- Loading Template -->
    <ng-template #loadingTemplate>
      <div class="d-flex justify-content-center align-items-center" style="height: 300px;">
        <mat-spinner></mat-spinner>
        <span class="ms-3">Loading dashboard stats...</span>
      </div>
    </ng-template>
  `,
  styles: [`
    .stat-card {
      border-left: 4px solid var(--bs-primary);
      transition: transform 0.2s ease-in-out;
    }

    .stat-card:hover {
      transform: translateY(-2px);
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
    }

    .stat-icon mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .stat-info h3, .stat-info h4 {
      font-weight: 600;
      color: #333;
    }

    .bg-primary { background-color: #0d6efd !important; }
    .bg-success { background-color: #198754 !important; }
    .bg-warning { background-color: #ffc107 !important; }
    .bg-info { background-color: #0dcaf0 !important; }
    .bg-secondary { background-color: #6c757d !important; }
    .bg-danger { background-color: #dc3545 !important; }
    .bg-dark { background-color: #212529 !important; }

    mat-card-content {
      padding: 20px !important;
    }

    .btn-sm mat-icon {
      vertical-align: middle;
    }

    .text-primary { color: #0d6efd !important; }
  `],
})
export class AdminDashboardComponent implements OnInit {
  loading = true;
  stats: DashboardStats = {
    totalUsers: 0,
    activeUsers: 0,
    pendingVerifications: 0,
    firstTimeLogins: 0,
    newUsersThisMonth: 0,
    adminUsers: 0,
    regularUsers: 0
  };

  ngOnInit() {
    this.loadDashboardStats();
  }

  loadDashboardStats() {
    this.loading = true;

    // Simulate API call - replace with actual service call
    setTimeout(() => {
      this.stats = {
        totalUsers: 1247,
        activeUsers: 1089,
        pendingVerifications: 158,
        firstTimeLogins: 42,
        newUsersThisMonth: 89,
        adminUsers: 15,
        regularUsers: 1232
      };
      this.loading = false;
    }, 1500);
  }

  refreshStats() {
    this.loadDashboardStats();
  }

  getPercentage(value: number, total: number): string {
    return total > 0 ? ((value / total) * 100).toFixed(1) : '0';
  }
}
