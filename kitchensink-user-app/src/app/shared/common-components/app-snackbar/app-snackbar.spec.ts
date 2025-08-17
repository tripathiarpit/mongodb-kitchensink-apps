import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppSnackbar } from './app-snackbar';

describe('AppSnackbar', () => {
  let component: AppSnackbar;
  let fixture: ComponentFixture<AppSnackbar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppSnackbar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppSnackbar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
