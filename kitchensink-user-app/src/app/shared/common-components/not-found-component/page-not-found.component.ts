import { Component } from '@angular/core';
import {MaterialModule} from '../../../material.module';
import {Router} from '@angular/router';

@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrls: ['./page-not-found.component.scss'],
  imports:[MaterialModule]
})
export class PageNotFoundComponent {
  constructor(private router:Router) {
  }
  goToHome() {
    this.router.navigate(['/dashboard']);
  }
}

