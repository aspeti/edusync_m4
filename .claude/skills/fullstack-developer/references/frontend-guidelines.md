# Frontend guidelines — Angular 21 + PrimeNG (EduSync)

## Stack (confirmed)

- Angular **21** standalone SPA; TypeScript ~5.9; RxJS 7.8; SCSS.
- PrimeNG **21.x** + `@angular/cdk` + `@primeuix/themes` (`ADR-0026`).
- Theme: `core/ui/edusync-theme.ts` + `_tokens.scss` (`ADR-0015`).
- Auth: JWT in `sessionStorage`; `authInterceptor`; role guards.
- Tests: Vitest; format: Prettier. Build: `ng build`.

## Do

- Place screens under `frontend/src/app/features/<area>/`.
- Reuse `PageResponse<T>`, list filter/pagination patterns from usuarios/plataforma/academico.
- Prefer PrimeNG for forms, dialogs, toasts, and data tables (`p-table` for notes/centralizadores).
- Keep grade matrix display-only for averages returned by API; client validates range for UX only.
- Lazy-load routes; keep shell nav role-gated like existing `shell.component.ts`.
- Match existing minimal “no design system card soup” pages unless migrating a screen to PrimeNG intentionally.

## Don’t

- Add Angular Material, AG Grid, another UI kit, or NgRx without a new ADR.
- Use PrimeNG **22.x** (requires Angular 22).
- Compute `CalculoNotas` (section/period/gestion averages) in the browser.
- Store JWT in `localStorage` (project convention: `sessionStorage`).
- Log PII / RUDE / tokens in console in committed code.

## Typical change checklist

1. Models + service methods aligned to backend DTOs.
2. Page/component + routes + shell link if needed.
3. `ng build`; add Vitest when pure logic is extracted (e.g. grid helpers).
4. Sync DTP / DD status if this closes a `PR-IMPL`.
