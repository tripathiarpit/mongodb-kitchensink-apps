
import {Component, OnInit, ViewChild, OnDestroy, inject} from '@angular/core';
import {NavigationEnd, Router, RouterLink, RouterOutlet} from '@angular/router';
import {AuthService} from '../../core/services/AuthService';
import {NavigationMenu, NavItem} from '../../shared/model/NaigationModel';
import {MaterialModule} from '../../material.module';
import {AppFooterComponent} from '../../shared/common-components/app-footer.component';
import {MatSidenav} from '@angular/material/sidenav';
import {AppSettings, AppSettingsService} from '../../core/services/AppSettingsService';
import {filter, Subject, takeUntil} from 'rxjs';
import {Title} from '@angular/platform-browser';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [MaterialModule, RouterOutlet]
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private settingsService = inject(AppSettingsService);

  menuItems: NavItem[] = [];
  userRole: string | null = '' ;
  currentUserRole: string = 'USER';
  roles: string[]= [];
  @ViewChild('sidenav') sidenav!: MatSidenav;
  isMobile = false;
  isOpened = true;
  currentUserFullName: string | null ="";
  isCollapsed = false;
  showWelcome = true;
  pageTitle = '';
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

  constructor(private authService: AuthService, private router: Router, private titleService: Title) {}

  ngOnInit() {
    this.userRole = this.authService.getUserRole();

    this.currentUserFullName = this.authService.getFullName();
    this.loadMenu();
    this.loadSettings();
    const rolesString = this.authService.getUserRole();
    const roles: string[] = rolesString ? JSON.parse(rolesString) : [];
    this.roles = roles;
    this.userRole = roles.join(",");
    if (roles.includes('ADMIN')) {
      this.router.navigate(['dashboard/admin']);
    } else if (roles.includes('USER')) {
      this.router.navigate(['/dashboard/user-details'], { state: { email: this.authService.getEmail() } });
    } else {
      // fallback for unknown role
      this.router.navigate(['/access-denied']);
    }
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((data) => {
        this.pageTitle = this.titleService.getTitle()
        console.log('Page title:', this.pageTitle);
      });
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
    this.menuItems = NavigationMenu.allMenuItems.filter(item =>
      this.authService.isAuthorized(item.allowedRoles)
    );
  }

  gotoHome(): void {
    this.router.navigate(['dashboard']);
  }

  onMenuClick(route: string | undefined) {
    if (!this.authService.isLoggedIn()) {
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
    return this.currentSettings.darkMode ? 'virbrant' : 'light-theme';
  }

  // Helper method to get current time for welcome section
  getCurrentTime(): string {
    return new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
  }
}
