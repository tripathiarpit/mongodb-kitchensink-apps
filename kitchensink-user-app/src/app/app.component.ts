import {Component, OnInit} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import { MaterialModule } from './material.module';
import {IdleTimeoutDialogComponent} from './shared/common-components/timeout-component';
import {IdleTimeoutService} from './core/services/IdleTimeoutService';
import {AuthService} from './core/services/AuthService';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-root',
  template: `
    <mat-toolbar color="primary" class="app-header" fxLayout="row" fxLayoutAlign="start center" style="padding: 0 16px; height: 56px;">
      <!-- Left: Logo or App Name -->
      <div class="app-logo" style="display: flex; align-items: center; cursor: pointer;">
        <mat-icon style="margin-right: 8px;">dashboard</mat-icon>
        <span class="app-title" style="font-weight: 600; font-size: 20px; letter-spacing: 1px;">
      Kitchensink User App
    </span>
      </div>
    </mat-toolbar>

    <!-- Content area fills remaining viewport -->
    <div class="app-content">
      <router-outlet></router-outlet>
    </div>

    <app-idle-timeout-dialog [visible]="showWarning" [countdown]="countdown"></app-idle-timeout-dialog>

  `,
 styleUrls: ['./app.component.scss'],
  imports: [RouterOutlet, MaterialModule, IdleTimeoutDialogComponent]
})
export class AppComponent implements  OnInit {
  showWarning = false;
  countdown = 10;
  countdownInterval: any;
  private subs: Subscription[] = [];
  constructor(private router: Router,private idleService: IdleTimeoutService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    // Start/stop watching based on login state
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

    // When idleService requests a warning, show dialog and start countdown
    this.subs.push(
      this.idleService.warning$.subscribe(() => {
        this.showWarning = true;
        this.countdown = 5; // warning duration in seconds
        this.startCountdown();
      })
    );

    // When idleService emits final timeout, logout immediately
    this.subs.push(
      this.idleService.timeout$.subscribe(() => {
        this.performLogout();
      })
    );

    // If user presses key or moves mouse while warning is shown, reset
    window.addEventListener('keydown', () => this.onUserActivityDuringWarning());
    window.addEventListener('mousemove', () => this.onUserActivityDuringWarning());
    window.addEventListener('click', () => this.onUserActivityDuringWarning());
    window.addEventListener('touchstart', () => this.onUserActivityDuringWarning());
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
    // hide UI and restart idle watch (this will cancel pending timeout in service)
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
  goHome() {
    // Example: Navigate to home or user list page
    this.router.navigate(['/user']);
  }

  onProfileClick() {
    // Example: Open profile dialog or navigate to profile
    console.log('Profile clicked');
  }


}
