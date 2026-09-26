// La version AUDITADA de generado-politica.spec.ts: mismos pasos, sin los tres defectos.
import { test, expect } from '@playwright/test';

test.describe('Envío de consultas', () => {
  test('Consulta de política (auditado)', async ({ page }) => {
    await page.goto('/');
    await page.getByLabel('Tu consulta').fill('cuántos días tengo para devolver');
    await page.getByRole('button', { name: 'Enviar' }).click();

    await expect(page.getByTestId('mensaje-asistente')).not.toBeEmpty();
    await expect(page.getByTestId('citas')).toHaveText('politica_devoluciones.md');
    await expect(page.getByTestId('intencion')).toHaveText('politica');
  });
});
