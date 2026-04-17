import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AuthGuard } from './auth-guard';

describe('authGuard', () => {
  let guard: AuthGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])]
    });
    guard = TestBed.inject(AuthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
