import { Component, OnInit } from '@angular/core';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MaterialModule} from '../../material.module';
import {DashboardService} from '../../core/services/DashboardService';
import {DashboardStats} from '../../shared/model/DashboardStatsModel';
import {LoaderService} from '../../core/services/LoaderService';
import { CommonModule } from '@angular/common';
import {finalize} from 'rxjs/operators'; // Import CommonModule for Angular pipes

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

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    MaterialModule,
    MatProgressSpinner,
    CommonModule // Required for pipes like DatePipe, NumberPipe
  ],
  template: `<div class="container-fluid py-4 bg-light min-vh-100">
    <!-- Dashboard Header -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="d-flex align-items-center justify-content-between mb-4">
          <div>
            <h2 class="text-dark fw-bold mb-1">Admin Dashboard</h2>
            <p class="text-muted mb-0">Monitor your application statistics</p>
          </div>
          <mat-icon class="text-primary fs-1">dashboard</mat-icon>
        </div>
      </div>
    </div>

    <!-- Dashboard Stats -->
    <div *ngIf="!loading; else loadingTemplate">

      <!-- User Base Overview Section -->
      <div class="row mb-4">
        <div class="col-12">
          <h5 class="text-dark fw-semibold mb-3 d-flex align-items-center">
            <mat-icon class="me-2 text-primary">groups</mat-icon>
            User Base Overview
          </h5>
        </div>
      </div>

      <div class="row g-4 mb-5">
        <!-- Total Users Card -->
        <div class="col-xl-3 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">Total Users</p>
                  <h3 class="text-primary fw-bold mb-0">{{ stats.totalUsers }}</h3>
                </div>
                <div class="bg-primary bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-primary fs-4">groups</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Active Users Card -->
        <div class="col-xl-3 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">Active Users</p>
                  <h3 class="text-success fw-bold mb-0">{{ stats.activeUsers }}</h3>
                </div>
                <div class="bg-success bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-success fs-4">how_to_reg</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Pending Verifications Card -->
        <div class="col-xl-3 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">Pending Verifications</p>
                  <h3 class="text-warning fw-bold mb-0">{{ stats.pendingVerifications }}</h3>
                </div>
                <div class="bg-warning bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-warning fs-4">pending_actions</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- First Time Logins Card -->
        <div class="col-xl-3 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">First Time Logins</p>
                  <h3 class="text-info fw-bold mb-0">{{ stats.firstTimeLogins }}</h3>
                </div>
                <div class="bg-info bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-info fs-4">login</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>

      <!-- User Roles Breakdown Section -->
      <div class="row mb-4">
        <div class="col-12">
          <h5 class="text-dark fw-semibold mb-3 d-flex align-items-center">
            <mat-icon class="me-2 text-primary">admin_panel_settings</mat-icon>
            User Roles Breakdown
          </h5>
        </div>
      </div>

      <div class="row g-4 mb-4">
        <!-- New Users This Month Card -->
        <div class="col-xl-4 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">New Users (This Month)</p>
                  <h3 class="text-secondary fw-bold mb-0">{{ stats.newUsersThisMonth }}</h3>
                </div>
                <div class="bg-secondary bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-secondary fs-4">person_add</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Admin Users Card -->
        <div class="col-xl-4 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">Admin Users</p>
                  <h3 class="text-danger fw-bold mb-0">{{ stats.adminUsers }}</h3>
                </div>
                <div class="bg-danger bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-danger fs-4">admin_panel_settings</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Regular Users Card -->
        <div class="col-xl-4 col-md-6">
          <mat-card class="h-100 border-0 shadow-sm">
            <mat-card-content class="p-4">
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <p class="text-muted small mb-1 text-uppercase fw-medium">Regular Users</p>
                  <h3 class="text-dark fw-bold mb-0">{{ stats.regularUsers }}</h3>
                </div>
                <div class="bg-dark bg-opacity-10 rounded-circle p-3">
                  <mat-icon class="text-dark fs-4">group</mat-icon>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>

    </div>

    <!-- Loading Template -->
    <ng-template #loadingTemplate>
      <div class="d-flex flex-column justify-content-center align-items-center" style="height: 50vh;">
        <mat-spinner diameter="60" color="primary"></mat-spinner>
        <p class="mt-4 text-muted fw-medium">Loading dashboard statistics...</p>
      </div>
    </ng-template>
  </div>`,
  styles: [`
    /* Base stat card styling */
    mat-card {
      transition: all 0.3s ease;
      border-radius: 12px !important;
    }

    mat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
    }

    /* Icon Container Styling */
    .rounded-circle {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    /* Typography Enhancements */
    h2.fw-bold {
      font-size: 2rem;
    }

    h3.fw-bold {
      font-size: 1.75rem;
    }

    h5.fw-semibold {
      font-size: 1.1rem;
    }

    /* Section Headers */
    h5.fw-semibold mat-icon {
      font-size: 1.25rem;
    }

    /* Main Dashboard Icon */
    .fs-1 {
      font-size: 2.5rem !important;
    }

    /* Card Icon Sizing */
    .fs-4 {
      font-size: 1.5rem !important;
    }

    /* Responsive Adjustments */
    @media (max-width: 768px) {
      h2.fw-bold {
        font-size: 1.5rem;
      }

      h3.fw-bold {
        font-size: 1.25rem;
      }

      .rounded-circle {
        width: 48px;
        height: 48px;
      }

      .fs-4 {
        font-size: 1.25rem !important;
      }
    }

    /* Loading State */
    mat-spinner {
      margin-bottom: 1rem;
    }
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
    serverTime: '',
    adminUsers: 0,
    regularUsers: 0,
    avgResponseTimeMs: 0,
    uptimeSeconds: 0
  };

  constructor(private dashboardService: DashboardService, private loader: LoaderService) {}

  ngOnInit() {
    this.loadDashboardStats();
  }


loadDashboardStats() {
  this.loader.show();
  this.dashboardService.getDashboardStats().pipe(
    finalize(() => {
      this.loader.hide();
    })
  ).subscribe({
    next: (data) => {
      this.stats = data;
      this.loading = false;
      // No need to call this.loader.hide() here
    },
    error: (err) => {
      console.error('Failed to load dashboard stats:', err);
      // No need to call this.loader.hide() here
    },
    complete: () => {
      console.log('Dashboard stats request completed');
      // No need to call this.loader.hide() here
    }
  });
}

  refreshStats() {
    this.loadDashboardStats();
  }

  getPercentage(value: number, total: number): string {
    return total > 0 ? ((value / total) * 100).toFixed(1) : '0';
  }
}
