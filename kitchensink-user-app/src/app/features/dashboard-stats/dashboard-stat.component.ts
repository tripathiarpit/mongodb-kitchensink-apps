import { Component, OnInit } from '@angular/core';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MaterialModule} from '../../material.module';
import {DashboardService} from '../../core/services/DashboardService';
import {DashboardStats} from '../../shared/model/DashboardStatsModel';
import {LoaderService} from '../../core/services/LoaderService';
import { CommonModule } from '@angular/common'; // Import CommonModule for Angular pipes

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
  template: `
    <div class="container-fluid p-4">
      <!-- Dashboard Header -->
      <div class="row mb-6"> <!-- Increased bottom margin for header -->
        <div class="col-12">
          <h2 class="text-primary mb-0 flex items-center">
            <mat-icon class="mr-3 text-4xl">dashboard</mat-icon> <!-- Larger icon for title -->
            <span class="text-3xl font-extrabold">Admin Dashboard</span> <!-- Larger, bolder title -->
          </h2>
          <p class="text-muted text-lg mt-2">System overview and key metrics</p> <!-- Larger subtitle -->
        </div>
      </div>

      <!-- Dashboard Stats Flex Containers -->
      <div *ngIf="!loading; else loadingTemplate">

        <!-- Category 1: User Base Overview -->
        <h3 class="text-xl font-semibold mb-3 mt-6 text-gray-800">User Base Overview</h3>
        <div class="flex flex-wrap justify-start gap-custom p-2 mb-6">
          <!-- Total Users Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-blue-500 mr-3 icon-background-blue">groups</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">Total Users</h3>
                <span class="text-xl font-bold text-blue-700">{{ stats.totalUsers }}</span>
              </div>
            </div>
          </mat-card>

          <!-- Active Users Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-green-500 mr-3 icon-background-green">how_to_reg</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">Active Users</h3>
                <span class="text-xl font-bold text-green-700">{{ stats.activeUsers }}</span>
              </div>
            </div>
          </mat-card>

          <!-- Pending Verifications Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-yellow-500 mr-3 icon-background-yellow">pending_actions</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">Pending Verifications</h3>
                <span class="text-xl font-bold text-yellow-700">{{ stats.pendingVerifications }}</span>
              </div>
            </div>
          </mat-card>

          <!-- First Time Logins Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-purple-500 mr-3 icon-background-purple">login</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">First Time Logins</h3>
                <span class="text-xl font-bold text-purple-700">{{ stats.firstTimeLogins }}</span>
              </div>
            </div>
          </mat-card>
        </div>

        <!-- Category 2: User Roles Breakdown -->
        <h3 class="text-xl font-semibold mb-3 mt-6 text-gray-800">User Roles Breakdown</h3>
        <div class="flex flex-wrap justify-start gap-custom p-2 mb-6">
          <!-- New Users This Month Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-indigo-500 mr-3 icon-background-indigo">person_add</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">New Users (This Month)</h3>
                <span class="text-xl font-bold text-indigo-700">{{ stats.newUsersThisMonth }}</span>
              </div>
            </div>
          </mat-card>

          <!-- Admin Users Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-red-500 mr-3 icon-background-red">admin_panel_settings</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">Admin Users</h3>
                <span class="text-xl font-bold text-red-700">{{ stats.adminUsers }}</span>
              </div>
            </div>
          </mat-card>

          <!-- Regular Users Card -->
          <mat-card class="shadow-sm rounded-xl stat-card">
            <div class="flex items-center p-2 h-full">
              <mat-icon class="text-4xl text-teal-500 mr-3 icon-background-teal">group</mat-icon>
              <div class="flex-grow">
                <h3 class="text-sm font-medium text-gray-700">Regular Users</h3>
                <span class="text-xl font-bold text-teal-700">{{ stats.regularUsers }}</span>
              </div>
            </div>
          </mat-card>
        </div>


      </div>

      <!-- Loading Template -->
      <ng-template #loadingTemplate>
        <div class="flex flex-col justify-center items-center h-96 bg-gray-50 rounded-lg shadow-inner">
          <mat-spinner diameter="60" color="primary"></mat-spinner>
          <span class="mt-5 text-xl font-medium text-gray-700">Loading dashboard statistics...</span>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    /* Base stat card styling */
    .stat-card {
      width: 288px;  /* Approximately 3 inches */
      height: 96px; /* Approximately 1 inch */
      transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
      border: 1px solid #e2e8f0; /* Light border for separation */
      display: flex; /* Use flexbox for internal alignment */
      align-items: center; /* Center content vertically */
      justify-content: center; /* Center content horizontally */
      box-sizing: border-box; /* Include padding and border in the element's total width and height */
    }

    .stat-card:hover {
      transform: translateY(-2px); /* Subtle lift on hover */
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); /* Slightly more pronounced shadow */
    }

    /* Custom gap for flex containers */
    .gap-custom {
      gap: 3px;
      display: flex;
    }

    /* Icon background styling */
    .icon-background-blue {
      background-color: #DBEAFE; /* Light blue Tailwind 100 */
      border-radius: 9999px; /* Full circle */
      padding: 0.75rem; /* Adjusted padding to control size of the circle for 1-inch height */
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0; /* Prevent icon from shrinking */
    }
    .icon-background-green {
      background-color: #D1FAE5; /* Light green Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-yellow {
      background-color: #FEF3C7; /* Light yellow Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-purple {
      background-color: #EDE9FE; /* Light purple Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-indigo {
      background-color: #EEF2FF; /* Light indigo Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-red {
      background-color: #FEE2E2; /* Light red Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-teal {
      background-color: #CCFBF1; /* Light teal Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-gray {
      background-color: #F3F4F6; /* Light gray Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-orange {
      background-color: #FFEDD5; /* Light orange Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .icon-background-cyan {
      background-color: #CFFAFE; /* Light cyan Tailwind 100 */
      border-radius: 9999px;
      padding: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    /* Adjust mat-icon size within the circular background */
    .icon-background-blue mat-icon,
    .icon-background-green mat-icon,
    .icon-background-yellow mat-icon,
    .icon-background-purple mat-icon,
    .icon-background-indigo mat-icon,
    .icon-background-red mat-icon,
    .icon-background-teal mat-icon,
    .icon-background-gray mat-icon,
    .icon-background-orange mat-icon,
    .icon-background-cyan mat-icon {
      font-size: 2rem; /* Equivalent to text-3xl, fitting the smaller circle */
      width: 2rem;
      height: 2rem;
    }

    /* Bootstrap compatibility classes - keep these as they are used in the template */
    .text-primary { color: #0d6efd !important; } /* Bootstrap primary color */
    .text-muted { color: #6c757d !important; }   /* Bootstrap muted text */

    /* Global Theme Styles - Integrated (assuming these are in your global styles.css) */
    /*
    :root {
      --base-font-size: 14px;
      --primary-color: #3f51b5;
      --background-color: #fafafa;
      --surface-color: #ffffff;
      --text-color: #212121;
      --text-secondary: #757575;
      --border-color: #e0e0e0;
    }

    * {
      font-size: var(--base-font-size) !important;
    }

    .light-theme {
      --background-color: #fafafa;
      --surface-color: #ffffff;
      --text-color: #212121;
      --text-secondary: #757575;
      --border-color: #e0e0e0;
      background-color: var(--background-color);
      color: var(--text-color);
    }

    .virbrant-theme {
      --background-color: burlywood;
      --surface-color: cadetblue;
      --text-color: #ffffff;
      --text-secondary: #ff6ec7;
      --border-color: #9d4edd;

      background-color: var(--background-color);
      color: var(--text-color);
    }
    .theme-indigo { --primary-color: #3f51b5; }
    .theme-pink { --primary-color: #e91e63; }
    .theme-purple { --primary-color: #9c27b0; }
    .theme-deep-purple { --primary-color: #673ab7; }
    .theme-blue { --primary-color: #2196f3; }
    .theme-teal { --primary-color: #009688; }
    .theme-green { --primary-color: #4caf50; }
    .theme-orange { --primary-color: #ff9800; }

    .mat-app-background {
      background-color: var(--background-color);
      color: var(--text-color);
    }

    .mat-mdc-card {
      background-color: var(--surface-color) !important;
      color: var(--text-color) !important;
    }

    .mat-toolbar {
      background-color: var(--primary-color) !important;
      color: var(--text-color) !important;
    }
    */
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
