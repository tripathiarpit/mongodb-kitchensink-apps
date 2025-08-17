import {Component, OnInit, OnDestroy} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import { MaterialModule } from '../material.module';
import {IdleTimeoutDialogComponent} from '../shared/common-components/timeout-component';
import {IdleTimeoutService} from '../core/services/IdleTimeoutService';
import {AuthService} from '../core/services/AuthService';
import {Subscription} from 'rxjs';
import {LoaderComponent} from '../shared/common-components/loader/loader.component';
import {AppSettings, AppSettingsService} from '../core/services/AppSettingsService';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [RouterOutlet, MaterialModule, IdleTimeoutDialogComponent, LoaderComponent]
})
export class AppComponent implements OnInit, OnDestroy {
  showWarning = false;
  countdown = 10;
  countdownInterval: any;
  private subs: Subscription[] = [];

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
    colorClassList: []
  };

  constructor(
    private router: Router,
    private idleService: IdleTimeoutService,
    private authService: AuthService,
    private settingsService: AppSettingsService // Add settings service
  ) {}

  ngOnInit(): void {
    this.subs.push(
      this.settingsService.getSettingsObservable().subscribe(settings => {
        this.currentSettings = settings;
      })
    );

    this.subs.push(
      this.authService.isLoggedIn$.subscribe(isLoggedIn => {
        if (isLoggedIn) {
          this.idleService.startWatching();
        } else {
          this.idleService.stopWatching();
          this.hideWarning();
        }
      })
    );


    this.subs.push(
      this.idleService.warning$.subscribe(() => {
        this.showWarning = true;
        this.countdown = 30;
        this.startCountdown();
      })
    );

    this.subs.push(
      this.idleService.timeout$.subscribe(() => {
        this.performLogout();
      })
    );

    window.addEventListener('keydown', () => this.onUserActivityDuringWarning());
    window.addEventListener('mousemove', () => this.onUserActivityDuringWarning());
    window.addEventListener('click', () => this.onUserActivityDuringWarning());
    window.addEventListener('touchstart', () => this.onUserActivityDuringWarning());
  }

  navigateToSettings() {
    this.router.navigate(['dashboard/settings']);
  }

  toggleTheme() {
    this.settingsService.setDarkMode(!this.currentSettings.darkMode);
  }

  goHome() {
    const rolesString = this.authService.getUserRole();
    const roles: string[] = rolesString ? JSON.parse(rolesString) : [];

    if (roles.includes('ADMIN')) {
      this.router.navigate(['dashboard/admin']);
    } else if (roles.includes('USER')) {
      this.router.navigate(['/dashboard/user-details'], { state: { email: this.authService.getEmail() } });
    } else {
      this.router.navigate(['/access-denied']);
    }
  }

  private startCountdown() {
    this.clearCountdownInterval();
    this.countdownInterval = setInterval(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        this.clearCountdownInterval();
        this.performLogout();
      }
    }, 1000);
  }

  private clearCountdownInterval() {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
  }

  private onUserActivityDuringWarning() {
    if (!this.showWarning) return;
    this.hideWarning();
    this.idleService.startWatching();
  }

  private hideWarning() {
    this.showWarning = false;
    this.clearCountdownInterval();
  }

  private performLogout() {
    this.hideWarning();
    this.authService.logout();
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.clearCountdownInterval();
  }
}
