import { Routes } from '@angular/router';
import { UserListComponent } from './features/users/components/user-list/user-list.component';
import { LoginComponent } from './features/auth/login-component';
import { SignupComponent } from './features/users/components/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard-component';
import { PageNotFoundComponent } from './shared/common-components/not-found-component/page-not-found.component';
import { AuthGuard } from './core/services/AuthGaurd';
import { ForgotPasswordComponent } from './features/auth/reset-password/forgot-password-component';
import { UserDetailsComponent } from './features/users/components/user-details/user-details.component';
import { EditUserComponent } from './features/users/components/edit-user/edit-user.component';
import { AccessDeniedComponent } from './features/auth/access-denied/access-denied.component';
import {EditProfileComponent} from './features/users/components/edit-profile/edit-profile.component';
import {AccessGuard} from './core/services/AccessGaurd';
import {AppSettingsComponent} from './shared/common-components/app-settings-component/app-settings-component';
import {AdminDashboardComponent} from './features/dashboard-stats/dashboard-stat.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },

  // Public routes
  {
    path: 'login',
    component: LoginComponent,
    title: 'Login Page'
  },
  {
    path: 'signup',
    component: SignupComponent,
    title: 'Signup Page'
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    title: 'Forgot Password'
  },
  {
    path: 'access-denied',
    component: AccessDeniedComponent,
    title: 'Access Denied'
  },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    title: 'Dashboard',
    data: { allowedRoles: ['ADMIN','USER'] },
    children: [
      {
        path: '',
        redirectTo: 'admin',
        pathMatch: 'full'
      },
      {
        path: 'admin',
        component: AdminDashboardComponent,
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN'] },
        title: 'Admin Dashboard'
      },
      {
        path: 'user-management',
        component: UserListComponent,
        title: 'User Management'
      },
      {
        path: 'edit-user/:email',
        component: EditUserComponent,
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN'] },
        title: 'Edit User'
      },
      {
        path: 'edit-user',
        component: EditUserComponent,
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN'] },
        title: 'Edit User'
      },
      {
        path: 'edit-profile/:email',
        component: EditProfileComponent,
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN','USER'] },
        title: 'Edit User'
      },
      {
        path: 'edit-profile',
        component: EditProfileComponent,
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN','USER'] },
        title: 'Edit User'
      },
      {
        path: 'user-details/:id',
        component: UserDetailsComponent,
        title: 'User Details'
      },
      {
        path: 'user-details',
        component: UserDetailsComponent,
        title: 'User Details'
      },
      {
        path: 'forgot-password',
        component: ForgotPasswordComponent,
        title: 'Forgot Password'
      },
      {
        path: 'settings',
        component: AppSettingsComponent,
      }
    ]
  },

  // 404 fallback
  {
    path: '**',
    component: PageNotFoundComponent,
    title: 'Page Not Found'
  }
];
