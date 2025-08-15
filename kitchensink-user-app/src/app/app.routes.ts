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
import { AccessGaurd } from './core/services/AccessGaurd';
import {EditProfileComponent} from './features/users/components/edit-profile/edit-profile.component';

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

  // Protected routes
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    title: 'Dashboard',
    data: { allowedRoles: ['ADMIN','USER'] },
    children: [
      {
        path: 'user-management',
        component: UserListComponent,
        title: 'User Management'
      },
      {
        path: 'edit-user/:email',
        component: EditUserComponent,
        canActivate: [AccessGaurd],
        data: { allowedRoles: ['ADMIN'] },
        title: 'Edit User'
      },
      {
        path: 'edit-profile/:email',
        component: EditProfileComponent,
        canActivate: [AccessGaurd],
        data: { allowedRoles: ['ADMIN','USER'] },
        title: 'Edit User'
      },
      {
        path: 'user-details/:id',
        component: UserDetailsComponent,
        title: 'User Details'
      },
      {
        path: 'forgot-password',
        component: ForgotPasswordComponent,
        title: 'Forgot Password'
      },
    ]
  },

  // 404 fallback
  {
    path: '**',
    component: PageNotFoundComponent,
    title: 'Page Not Found'
  }
];
