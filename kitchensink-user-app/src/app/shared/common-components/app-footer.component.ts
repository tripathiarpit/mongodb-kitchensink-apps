import { Component } from '@angular/core';
import {MaterialModule} from '../../material.module';

@Component({
  selector: 'app-footer',
  template: `
    <footer class="footer">
      <div class="d-sm-flex justify-content-center justify-content-sm-between">
        <span class="text-muted d-block text-center text-sm-left d-sm-inline-block">Copyright © MongoDB</span>
        <span class="float-none float-sm-right d-block mt-1 mt-sm-0 text-center"> Free <a href="https://www.mongodb.com/" target="_blank">MongoDB</a></span>
      </div>
    </footer>

  `,
  styles: [],
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
