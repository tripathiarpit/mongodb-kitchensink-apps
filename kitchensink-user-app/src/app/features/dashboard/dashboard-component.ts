
import {Component, OnInit, ViewChild, OnDestroy, inject} from '@angular/core';
import {Router, RouterLink, RouterOutlet} from '@angular/router';
import {AuthService} from '../../core/services/AuthService';
import {NavigationMenu, NavItem} from '../../shared/model/NaigationModel';
import {MaterialModule} from '../../material.module';
import {AppFooterComponent} from '../../shared/common-components/app-footer.component';
import {NgForOf, NgIf} from '@angular/common';
import {MatSidenav} from '@angular/material/sidenav';
import {AppSettings, AppSettingsService} from '../../core/services/AppSettingsService';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [MaterialModule, RouterOutlet, AppFooterComponent, NgForOf, NgIf]
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private settingsService = inject(AppSettingsService);

  menuItems: NavItem[] = [];
  userRole: string | null = '' ;
  currentUserRole: string = 'USER';
  @ViewChild('sidenav') sidenav!: MatSidenav;
  isMobile = false;
  isOpened = true;
  currentUserFullName: string | null ="";
  isCollapsed = false;
  showWelcome = true;
  currentSettings: AppSettings = {
    darkMode: false,
    primaryColor: 'indigo',
    fontSize: 14,
    language: 'en',
    dateFormat: 'MM/dd/yyyy',
    notifications: { push: true, email: true, sound: true },
    animations: true,
    autoSave: true,
    analytics: false,
    rememberLogin: false,
    colorClassList:[]
  };

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.userRole = this.authService.getUserRole();
    this.currentUserFullName = this.authService.getFullName();
    this.loadMenu();
    this.loadSettings();
    const rolesString = this.authService.getUserRole();
    const roles: string[] = rolesString ? JSON.parse(rolesString) : [];

    if (roles.includes('ADMIN')) {
      this.router.navigate(['dashboard/admin']);
    } else if (roles.includes('USER')) {
      this.router.navigate(['/dashboard/user-details'], { state: { email: this.authService.getEmail() } });
    } else {
      // fallback for unknown role
      this.router.navigate(['/access-denied']);
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Load settings and subscribe to changes
  loadSettings() {
    this.settingsService.getSettingsObservable()
      .pipe(takeUntil(this.destroy$))
      .subscribe(settings => {
        this.currentSettings = settings;
      });
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
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate(['/dashboard/user-details'], { state: { email: this.authService.getEmail() } });
    });
  }

  toggleCollapse() {
    this.isCollapsed = !this.isCollapsed;
  }

  openSettings() {
    this.router.navigate(['dashboard/settings']);
  }

  get contentClass() {
    return `${this.themeClass} ${this.currentSettings.animations ? '' : 'no-animations'}`;
  }

  get themeClass() {
    return this.currentSettings.darkMode ? 'dark-theme' : 'light-theme';
  }

  // Helper method to get current time for welcome section
  getCurrentTime(): string {
    return new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
  }
}
