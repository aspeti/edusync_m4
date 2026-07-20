import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

/**
 * Helper base para llamadas HTTP.
 * La URL base {@code /api} se resuelve via el proxy de desarrollo Angular
 * ({@code proxy.conf.json} → {@code http://localhost:8080}).
 * DD-UC-004 §2.
 */
@Injectable({ providedIn: 'root' })
export class ApiBase {
  static readonly BASE = '/api/v1';

  constructor(readonly http: HttpClient) {}
}
