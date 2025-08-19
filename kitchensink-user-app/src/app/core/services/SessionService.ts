// src/app/core/services/SessionService.ts
import { Injectable } from '@angular/core';
import { AuthService } from './AuthService';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  constructor(private authService: AuthService) {}


  isSessionActive(): boolean {
    const token = this.authService.getAuthToken();
    return !!token;
  }
}
