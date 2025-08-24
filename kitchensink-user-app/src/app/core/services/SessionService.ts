// src/app/core/services/SessionService.ts
import { Injectable } from '@angular/core';
import { AuthService } from './AuthService';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class SessionService {
    constructor(private authService: AuthService) {}
  isSessionActive(): Observable<boolean> {
    return this.authService.isSessionActive();
  }
  isRefershSession(): Observable<boolean> {
    return this.authService.isSessionActive();
  }


}
