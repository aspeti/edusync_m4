import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { authGuard } from './auth.guard';
import { roleGuard } from './role.guard';
import { AuthService } from './auth.service';

/**
 * Unit tests de authGuard y roleGuard (DD-UC-004 §6).
 */
describe('authGuard', () => {
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    router = TestBed.inject(Router);
  });

  afterEach(() => sessionStorage.clear());

  it('bloquea acceso y redirige a /login si no autenticado', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );
    expect(result).toEqual(router.createUrlTree(['/login']));
  });
});

describe('roleGuard', () => {
  let router: Router;

  const fakeToken =
    'eyJhbGciOiJIUzI1NiJ9.' +
    btoa(JSON.stringify({ sub: 'sys@test.com', roles: ['SYSADMIN'], exp: 9999999999, iat: 1 }))
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '') +
    '.sig';

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    router = TestBed.inject(Router);
  });

  afterEach(() => sessionStorage.clear());

  it('redirige a /login si no autenticado', () => {
    const route = { data: { role: 'SYSADMIN' } } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );
    expect(result).toEqual(router.createUrlTree(['/login']));
  });

  it('permite acceso si el rol coincide', () => {
    sessionStorage.setItem('edusync_access_token', fakeToken);
    const route = { data: { role: 'SYSADMIN' } } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );
    expect(result).toBe(true);
  });

  it('redirige a /home si el rol no coincide', () => {
    sessionStorage.setItem('edusync_access_token', fakeToken);
    const route = { data: { role: 'ADMIN' } } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );
    expect(result).toEqual(router.createUrlTree(['/home']));
  });

  it('SYSADMIN (CSV del backend) no pasa el guard de ADMIN', () => {
    const csvToken =
      'eyJhbGciOiJIUzI1NiJ9.' +
      btoa(JSON.stringify({ sub: 'sys@test.com', roles: 'SYSADMIN', exp: 9999999999, iat: 1 }))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '') +
      '.sig';
    sessionStorage.setItem('edusync_access_token', csvToken);
    const route = { data: { role: 'ADMIN' } } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );
    expect(result).toEqual(router.createUrlTree(['/home']));
  });
});
