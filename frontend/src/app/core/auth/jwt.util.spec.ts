import { decodeJwtPayload, normalizeRoles } from './jwt.util';

describe('normalizeRoles', () => {
  it('parsea CSV del backend (JwtTokenProvider)', () => {
    expect(normalizeRoles('SYSADMIN')).toEqual(['SYSADMIN']);
    expect(normalizeRoles('ADMIN,SECRETARIA')).toEqual(['ADMIN', 'SECRETARIA']);
  });

  it('acepta array JSON (tests / payloads legacy)', () => {
    expect(normalizeRoles(['SYSADMIN'])).toEqual(['SYSADMIN']);
  });

  it('SYSADMIN no se confunde con ADMIN (regresión substring)', () => {
    const roles = normalizeRoles('SYSADMIN');
    expect(roles.includes('SYSADMIN')).toBe(true);
    expect(roles.includes('ADMIN')).toBe(false);
  });
});

describe('decodeJwtPayload', () => {
  function tokenWithPayload(payload: object): string {
    return (
      'eyJhbGciOiJIUzI1NiJ9.' +
      btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '') +
      '.sig'
    );
  }

  it('normaliza roles CSV a string[]', () => {
    const token = tokenWithPayload({
      sub: 'sys@edusync.local',
      roles: 'SYSADMIN',
      tenantId: '',
      exp: 9999999999,
      iat: 1,
    });
    const claims = decodeJwtPayload(token);
    expect(claims?.roles).toEqual(['SYSADMIN']);
    expect(claims?.roles.includes('ADMIN')).toBe(false);
  });
});
