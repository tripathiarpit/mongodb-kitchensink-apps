import { Component, OnInit } from '@angular/core';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MaterialModule} from '../../material.module';
import {DashboardService} from '../../core/services/DashboardService';
import {DashboardStats} from '../../shared/model/DashboardStatsModel';
import {LoaderService} from '../../core/services/LoaderService';

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
          <p class="text-muted">System overview and key metrics</p>
        </div>
      </div>
    </div>

    <div class="row g-3" *ngIf="!loading; else loadingTemplate">
      <div class="grid gap-4 p-4">
        <!-- Total Users -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>Total Users</h3>
              <span class="text-xl font-bold">{{ stats.totalUsers }}</span>
            </div>
          </mat-card>
        </div>

        <!-- Active Users -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>Active Users</h3>
              <span class="text-xl font-bold text-green-600">{{ stats.activeUsers }}</span>
            </div>
          </mat-card>
        </div>

        <!-- Pending Verifications -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>Pending Verifications</h3>
              <span class="text-xl font-bold text-yellow-600">{{ stats.pendingVerifications }}</span>
            </div>
          </mat-card>
        </div>

        <!-- First Time Logins -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>First Time Logins</h3>
              <span class="text-xl font-bold text-blue-600">{{ stats.firstTimeLogins }}</span>
            </div>
          </mat-card>
        </div>

        <!-- New Users This Month -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>New Users (This Month)</h3>
              <span class="text-xl font-bold text-purple-600">{{ stats.newUsersThisMonth }}</span>
            </div>
          </mat-card>
        </div>

        <!-- Admin Users -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>Admin Users</h3>
              <span class="text-xl font-bold text-red-600">{{ stats.adminUsers }}</span>
            </div>
          </mat-card>
        </div>

        <!-- Regular Users -->
        <div class="col-12 md:col-4">
          <mat-card class="shadow-md stat-card">
            <div class="flex items-center justify-between">
              <h3>Regular Users</h3>
              <span class="text-xl font-bold text-teal-600">{{ stats.regularUsers }}</span>
            </div>
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
    this.dashboardService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
        this.loader.hide();
      },
      error: (err) => {
        console.error('Failed to load dashboard stats:', err);
        this.loader.hide();

      },
      complete: () => {
        // Optional - fires when the observable completes
        console.log('Dashboard stats request completed');
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
