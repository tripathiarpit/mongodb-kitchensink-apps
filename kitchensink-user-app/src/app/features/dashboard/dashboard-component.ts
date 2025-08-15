
import {Component, OnInit, ViewChild} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {AuthService} from '../../core/services/AuthService';
import {NavigationMenu, NavItem} from '../../shared/model/NaigationModel';
import {MaterialModule} from '../../material.module';
import {AppFooterComponent} from '../../shared/common-components/app-footer.component';
import {NgForOf} from '@angular/common';
import {MatSidenav} from '@angular/material/sidenav';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [MaterialModule, RouterOutlet, AppFooterComponent, NgForOf]
})
export class DashboardComponent implements OnInit {
  menuItems: NavItem[] = [];
  userRole: string | null = '' ;
  currentUserRole: string = 'USER';
  @ViewChild('sidenav') sidenav!: MatSidenav;
  isMobile = false;
  isOpened = true;
  currentUserFullName: string | null ="";
  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.userRole = this.authService.getUserRole();
    this.currentUserFullName = this.authService.getFullName();
    this.loadMenu();
  }

  loadMenu() {
      this.menuItems = NavigationMenu.menuItems.filter(item =>
        this.authService.isAuthorized(item.allowedRoles)
      );
  }

  onMenuClick(route: string) {
    if (!this.authService.isLoggedIn()) {
      // redirect to login or show message
      this.router.navigate(['/login']);
      return;
    }
    // Navigate to the selected route inside dashboard main content
    this.router.navigate([`/dashboard/${route}`]);
  }
  toggleSidenav(): void {
    if (this.sidenav) {
      this.sidenav.toggle();
      this.isOpened = !this.isOpened;
    }
  }
  logout(): void {
    this.authService.logout();
}

  gotoUserProfile(): void {
    this.router.navigate(['dashboard/user-details', this.authService.getEmail()]);
  }
}
