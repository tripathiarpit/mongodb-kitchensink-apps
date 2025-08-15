// shared-state.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SharedStateService {
  private showSignInLinkSubject = new BehaviorSubject<boolean>(true);
  showSignInLink$ = this.showSignInLinkSubject.asObservable();
  setShowSignInLink(value: boolean) {
    this.showSignInLinkSubject.next(value);
  }
}
