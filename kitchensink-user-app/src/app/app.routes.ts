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
import { EditProfileComponent } from './features/users/components/edit-profile/edit-profile.component';
import { AccessGuard } from './core/services/AccessGaurd';
import { AppSettingsComponent } from './shared/common-components/app-settings-component/app-settings-component';
import { AdminDashboardComponent } from './features/dashboard-stats/dashboard-stat.component';
import { PublicGuard } from './core/services/PublicGuard';
import {AdminDashboardGuard} from './core/services/AdminDashboardGuard';
import {AppResourceSettingsComponent} from './features/app-settings/app-resource-settings';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },

  // Public routes, protected by PublicGuard to prevent authenticated users from navigating back.
  {
    path: 'login',
    component: LoginComponent,
    title: 'Login Page',
    canActivate: [PublicGuard],
  },
  {
    path: 'signup',
    component: SignupComponent,
    title: 'Signup Page',
    canActivate: [PublicGuard],
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    title: 'Forgot Password',
    canActivate: [PublicGuard],
  },
  {
    path: 'access-denied',
    component: AccessDeniedComponent,
    title: 'Access Denied',
  },

  // Protected Dashboard route and its children. AuthGuard checks for session.
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        redirectTo: 'user-details',
        pathMatch: 'full'
      },
      {
        path: 'admin-landing',
        canActivate: [AdminDashboardGuard],
        component: AdminDashboardComponent
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
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN'] },
        title: 'User Management'
      },
      {
        path: 'edit-user/:email',
        component: EditUserComponent,
        canActivate: [AccessGuard],
        data: { allowedRoles: ['ADMIN'] },
        title: 'Edit User',
      },
      {
        path: 'edit-user',
        component: EditUserComponent,
        canActivate: [AuthGuard, AccessGuard],
        data: { allowedRoles: ['ADMIN'] },
        title: 'Edit User',
      },
      {
        path: 'edit-profile/:email',
        component: EditProfileComponent,
        canActivate: [AuthGuard],
        data: { allowedRoles: ['ADMIN', 'USER'] },
        title: 'Edit Profile',
      },
      {
        path: 'edit-profile',
        component: EditProfileComponent,
        canActivate: [AuthGuard],
        data: { allowedRoles: ['ADMIN', 'USER'] },
        title: 'Edit Profile',
      },
      {
        path: 'user-details',
        component: UserDetailsComponent,
        canActivate: [AuthGuard],
        title: 'My Profile'
      },
      {
        path: 'user-details/:id',
        component: UserDetailsComponent,
        canActivate: [AuthGuard],
        title: 'User Details'
      },
      {
        path: 'settings',
        component: AppSettingsComponent,
        title: 'App Settings',
      },
      {
        path: 'resource-settings',
        component: AppResourceSettingsComponent,
        title: 'Resource Settings',
        canActivate: [AuthGuard],
        data: { allowedRoles: ['ADMIN'] },
      },
    ],
  },

  // 404 fallback - Must be the very last route in the array.
  {
    path: '**',
    component: PageNotFoundComponent,
    title: 'Page Not Found',
  },
];
