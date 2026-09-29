// Generado/auditado — caso 3.1 plan ADMIN.
import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';

test.describe('Escrituras ADMIN', () => {
  test('3.1 Alta de Curso con nombre único (auditado)', async ({ page }) => {
    const nombre = `Agente Curso ${Date.now()}`;

    await loginAsAdmin(page);
    await page.getByRole('link', { name: 'Cursos' }).click();
    await page.getByRole('link', { name: '+ Nuevo Curso' }).click();

    await page.getByLabel('Nombre').fill(nombre);
    await page.getByRole('button', { name: 'Crear Curso' }).click();

    await expect(page).toHaveURL(/\/academico\/cursos/);
    await page.getByPlaceholder('Buscar por nombre...').fill(nombre);
    await page.getByRole('button', { name: 'Buscar' }).click();
    await expect(page.getByText(nombre)).toBeVisible({ timeout: 10_000 });
  });
});
