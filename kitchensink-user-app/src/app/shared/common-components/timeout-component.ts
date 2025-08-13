import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-idle-timeout-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="overlay" *ngIf="visible">
      <div class="dialog">
        <h4>You're about to be logged out</h4>
        <p>You will be logged out in <strong>{{ countdown }}</strong> second{{ countdown === 1 ? '' : 's' }}.</p>
        <p>Press any key or move the mouse to continue your session.</p>
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0;
      display:flex; align-items:center; justify-content:center;
      background: rgba(0,0,0,0.45); z-index: 2000;
    }
    .dialog {
      background: #fff; padding: 20px 24px; border-radius: 8px; width: 320px;
      box-shadow: 0 6px 24px rgba(0,0,0,0.2); text-align:center;
    }
    h4 { margin: 0 0 8px; }
    p { margin: 8px 0; }
  `]
})
export class IdleTimeoutDialogComponent {
  @Input() visible = false;
  @Input() countdown = 5;

  onContinue() {
    // component only UI: parent should bind click via DOM events; but keep placeholder if needed
    const evt = new Event('keydown'); // trigger activity (parent resets on any key/mouse)
    window.dispatchEvent(evt);
  }
}
