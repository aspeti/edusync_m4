import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  const validToken =
    'eyJhbGciOiJIUzI1NiJ9.' +
    btoa(JSON.stringify({ sub: 'u', roles: 'SYSADMIN', exp: 9999999999, iat: 1 }))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '') +
    '.sig';

  beforeEach(() => {
    sessionStorage.clear();
    sessionStorage.setItem('edusync_access_token', validToken);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    // Fuerza que AuthService lea el token de sessionStorage
    TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('NO envía Bearer en POST /api/v1/auth/login', () => {
    http.post('/api/v1/auth/login', { email: 'a@b.c', password: 'x' }).subscribe();
    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ accessToken: validToken, expiresIn: 28800 });
  });

  it('NO envía Bearer en confirmación de reset de password', () => {
    http.post('/api/v1/auth/restablecer-password/confirmar', { token: 't', passwordNuevo: 'secreto12' }).subscribe();
    const req = httpMock.expectOne('/api/v1/auth/restablecer-password/confirmar');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('envía Bearer en endpoints autenticados', () => {
    http.get('/api/v1/plataforma/tenants').subscribe();
    const req = httpMock.expectOne('/api/v1/plataforma/tenants');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${validToken}`);
    req.flush([]);
  });
});
