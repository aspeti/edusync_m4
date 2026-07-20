import { TestBed, fakeAsync, tick } from '@angular/core/testing';
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
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '') +
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

  it('login guarda el token en sessionStorage y marca isAuthenticated', fakeAsync(() => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: fakeToken, expiresIn: 28800 });
    tick();

    expect(service.isAuthenticated()).toBe(true);
    expect(sessionStorage.getItem('edusync_access_token')).toBe(fakeToken);
  }));

  it('logout limpia el token y sessionStorage', fakeAsync(() => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: fakeToken, expiresIn: 28800 });
    tick();

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('edusync_access_token')).toBeNull();
  }));

  it('hasRole retorna true para SYSADMIN tras login', fakeAsync(() => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: fakeToken, expiresIn: 28800 });
    tick();

    expect(service.hasRole('SYSADMIN')).toBe(true);
    expect(service.hasRole('ADMIN')).toBe(false);
  }));

  it('mapea correctamente los claims del JWT', fakeAsync(() => {
    service.login({ email: 'test@test.com', password: 'pass' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush({ accessToken: fakeToken, expiresIn: 28800 });
    tick();

    const claims = service.claims();
    expect(claims?.sub).toBe('test@test.com');
    expect(claims?.roles).toContain('SYSADMIN');
  }));
});
