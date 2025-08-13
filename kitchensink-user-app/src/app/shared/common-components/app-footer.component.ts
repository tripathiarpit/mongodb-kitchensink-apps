import { Component } from '@angular/core';
import {MaterialModule} from '../../material.module';

@Component({
  selector: 'app-footer',
  template: `
    <mat-toolbar color="primary" class="dashboard-footer">
      <span>© 2025 Your Company</span>
    </mat-toolbar>
  `,
  styles: [`
    .dashboard-footer {
      position: sticky;
      bottom: 0;
      z-index: 2;
      justify-content: center;
    }
  `],
  imports:[MaterialModule]
})
export class AppFooterComponent {}
