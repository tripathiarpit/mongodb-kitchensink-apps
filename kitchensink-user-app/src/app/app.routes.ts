import { Routes } from '@angular/router';
import {UserListComponent} from './features/users/components/user-list/UserListComponent';
import {LoginComponent} from './features/auth/login-component';
import {SignupComponent} from './features/users/components/register/register.component';
import {DashboardComponent} from './features/dashboard/dashboard-component';
import {PageNotFoundComponent} from './shared/common-components/not-found-component/page-not-found.component';
import {AuthGuard} from './core/services/AuthGaurd';


export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, title: 'Login Page' },
  { path: 'signup', component: SignupComponent, title: 'Signup Page' },
  { path: 'user', component: UserListComponent, title: 'User List' },
  {path: 'dashboard',component: DashboardComponent, canActivate: [AuthGuard]},
  { path: '**', component: PageNotFoundComponent, title: 'Page Not Found' }
];
