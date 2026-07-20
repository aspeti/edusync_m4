import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { decodeJwtPayload, isTokenExpired, JwtPayload } from './jwt.util';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  expiresIn: number;
}

const TOKEN_KEY = 'edusync_access_token';

/**
 * Servicio de autenticación: login, logout y acceso a claims del JWT.
 * El token se almacena en {@code sessionStorage} (DD-UC-004 §2/§3, alternativa C):
 * se limpia al cerrar la pestaña/ventana; suficiente para el MVP.
 * NEVER usa localStorage.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _token = signal<string | null>(sessionStorage.getItem(TOKEN_KEY));

  readonly isAuthenticated = computed(() => {
    const token = this._token();
    if (!token) return false;
    const payload = decodeJwtPayload(token);
    return payload !== null && !isTokenExpired(payload);
  });

  readonly claims = computed<JwtPayload | null>(() => {
    const token = this._token();
    if (!token) return null;
    return decodeJwtPayload(token);
  });

  readonly roles = computed<string[]>(() => this.claims()?.roles ?? []);

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/v1/auth/login', request)
      .pipe(
        tap((response) => {
          sessionStorage.setItem(TOKEN_KEY, response.accessToken);
          this._token.set(response.accessToken);
        })
      );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    this._token.set(null);
  }

  getToken(): string | null {
    return this._token();
  }

  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }
}
