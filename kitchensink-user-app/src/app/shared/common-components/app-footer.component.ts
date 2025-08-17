import { Component } from '@angular/core';
import {MaterialModule} from '../../material.module';

@Component({
  selector: 'app-footer',
  template: `
    <div class="footer-container">
      <div class="footer-left">
        <span class="footer-text">© 2024 Kitchen Sink User App</span>
        <span class="footer-version">v1.0.0</span>
      </div>
      <div class="footer-right">
        <button mat-button class="footer-link" (click)="openLink('privacy')">
          <mat-icon>security</mat-icon>
          Privacy
        </button>
        <button mat-button class="footer-link" (click)="openLink('terms')">
          <mat-icon>description</mat-icon>
          Terms
        </button>
        <button mat-button class="footer-link" (click)="openLink('support')">
          <mat-icon>help</mat-icon>
          Support
        </button>
      </div>
    </div>
  `,
  styles: [`
    .footer-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      padding: 0;
      font-size: calc(var(--base-font-size, 14px) - 2px);
    }

      .footer-left {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .footer-text {
        color: var(--text-secondary, #666);
      }

      .footer-version {
        color: var(--primary-color, #1976d2);
        font-weight: 600;
        font-size: calc(var(--base-font-size, 14px) - 3px);
        background: var(--primary-color);
        padding: 2px 8px;
        border-radius: 4px;
      }

      .footer-right {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .footer-link {
        display: flex;
        align-items: center;
        gap: 4px;
        color: var(--text-secondary, #666);
        font-size: calc(var(--base-font-size, 14px) - 2px);
        min-width: auto;
        padding: 4px 12px;
        transition: all 0.3s ease;
      }

      .footer-link:hover {
        color: var(--primary-color, #1976d2);
        background: var(--primary-color, #1976d2) 10;
      }

      .footer-link mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }

      @media (max-width: 768px) {
        .footer-container {
          flex-direction: column;
          gap: 12px;
          text-align: center;
        }

        .footer-left {
          flex-direction: column;
          gap: 8px;
        }

        .footer-right {
          justify-content: center;
        }
      }
  `],
  imports:[MaterialModule]
})
export class AppFooterComponent {
  openLink(type: string) {
    switch (type) {
      case 'privacy':
        console.log('Opening Privacy Policy');
        break;
      case 'terms':
        console.log('Opening Terms of Service');
        break;
      case 'support':
        console.log('Opening Support');
        break;
    }
  }
}
