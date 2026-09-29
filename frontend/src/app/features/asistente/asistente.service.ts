import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiBase } from '../../core/api/api-base';
import {
  AgenteRequest,
  AgenteResponse,
  ContextoConsultaAgente,
  TurnoHistorialAgente,
} from './asistente.model';

/**
 * Cliente de POST /api/v1/ai/agente (DD-UC-024/025/026). No usa /chat ni /consultar-usuario.
 */
@Injectable({ providedIn: 'root' })
export class AsistenteService {
  constructor(private readonly api: ApiBase) {}

  consultar(
    pregunta: string,
    confirmed = false,
    history: TurnoHistorialAgente[] = [],
    contexto: ContextoConsultaAgente | null = null,
  ): Observable<AgenteResponse> {
    const body: AgenteRequest = { pregunta, confirmed, history, contexto };
    return this.api.http.post<AgenteResponse>(`${ApiBase.BASE}/ai/agente`, body);
  }
}
