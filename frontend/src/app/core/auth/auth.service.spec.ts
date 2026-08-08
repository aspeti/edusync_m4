import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

/**
 * Unit tests de {@link AuthService} (DD-UC-004 §6).
 * Verifica: login → sessionStorage; logout limpia; claims decodificados; hasRole.
 */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  // JWT real con payload { sub: 'test@test.com', roles: ['SYSADMIN'], exp: 9999999999 }
  const fakeToken =
    'eyJhbGciOiJIUzI1NiJ9.' +
    btoa(JSON.stringify({ sub: 'test@test.com', roles: ['SYSADMIN'], tenantId: null, exp: 9999999999, iat: 1 }))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '') +
    '.fakeSignature';

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('debería iniciar sin autenticación', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.getToken()).toBeNull();
  });

  it('login guarda el token en sessionStorage y marca isAuthenticated', () => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: fakeToken, expiresIn: 28800 });

    expect(service.isAuthenticated()).toBe(true);
    expect(sessionStorage.getItem('edusync_access_token')).toBe(fakeToken);
  });

  it('logout limpia el token y sessionStorage', () => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: fakeToken, expiresIn: 28800 });

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('edusync_access_token')).toBeNull();
  });

  it('hasRole retorna true para SYSADMIN tras login', () => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: fakeToken, expiresIn: 28800 });

    expect(service.hasRole('SYSADMIN')).toBe(true);
    expect(service.hasRole('ADMIN')).toBe(false);
  });

  it('mapea correctamente los claims del JWT', () => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: fakeToken, expiresIn: 28800 });

    const claims = service.claims();
    expect(claims?.sub).toBe('test@test.com');
    expect(claims?.roles).toContain('SYSADMIN');
  });

  it('hasRole no confunde SYSADMIN con ADMIN cuando roles vienen como CSV', () => {
    const csvToken =
      'eyJhbGciOiJIUzI1NiJ9.' +
      btoa(JSON.stringify({ sub: 'sys@edusync.local', roles: 'SYSADMIN', tenantId: '', exp: 9999999999, iat: 1 }))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '') +
      '.fakeSignature';

    service.login({ email: 'sys@edusync.local', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: csvToken, expiresIn: 28800 });

    expect(service.hasRole('SYSADMIN')).toBe(true);
    expect(service.hasRole('ADMIN')).toBe(false);
  });

  it('getToken limpia un JWT expirado de sessionStorage', () => {
    const expiredToken =
      'eyJhbGciOiJIUzI1NiJ9.' +
      btoa(JSON.stringify({ sub: 'u', roles: 'SYSADMIN', exp: 1, iat: 1 }))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '') +
      '.sig';

    sessionStorage.setItem('edusync_access_token', expiredToken);
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    expect(service.isAuthenticated()).toBe(false);
    expect(service.getToken()).toBeNull();
    expect(sessionStorage.getItem('edusync_access_token')).toBeNull();
  });
});
