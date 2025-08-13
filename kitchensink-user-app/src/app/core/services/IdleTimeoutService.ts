import { Injectable } from '@angular/core';
import { Subject, timer, Subscription } from 'rxjs';
import {AuthService} from './AuthService';

@Injectable({
  providedIn: 'root'
})
export class IdleTimeoutService {
  private totalIdleTime = 10_0000;
  private warningDuration = 5_000;
  private warnTimerSub?: Subscription;
  private timeoutTimerSub?: Subscription;
  private watching = false;

  // Emitted when warning should be shown
  warning$ = new Subject<void>();
  // Emitted when timeout occurs (final logout)
  timeout$ = new Subject<void>();
  private authSub!: Subscription;
  private idleInterval: any;
  isTracking = false;
  constructor(private authService: AuthService) {
    this.setupActivityListeners();
  }
  ngOnInit() {
    this.authSub = this.authService.isLoggedIn$.subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.startWatching();
      } else {
        this.stopWatching();
      }
    });
  }
  private setupActivityListeners() {
    // Reset only when watching
    ['mousemove', 'keydown', 'click', 'touchstart'].forEach(evt =>
      window.addEventListener(evt, () => {
        if (this.watching) {
          this.resetIdleTimer();
        }
      })
    );
  }

  startWatching() {
    if (this.watching) {
      this.resetIdleTimer();
      return;
    }
    this.watching = true;
    this.resetIdleTimer();
  }

  stopWatching() {
    this.watching = false;
    this.clearTimers();
  }

  private clearTimers() {
    this.warnTimerSub?.unsubscribe();
    this.warnTimerSub = undefined;
    this.timeoutTimerSub?.unsubscribe();
    this.timeoutTimerSub = undefined;
  }

  private resetIdleTimer() {
    if (!this.watching) return;

    this.clearTimers();

    const warnDelay = this.totalIdleTime - this.warningDuration; // when to show warning
    // Start timer to show warning
    this.warnTimerSub = timer(warnDelay).subscribe(() => {
      this.warning$.next();

      // Start final timeout (after warningDuration)
      this.timeoutTimerSub = timer(this.warningDuration).subscribe(() => {
        this.timeout$.next();
      });
    });
  }
}
