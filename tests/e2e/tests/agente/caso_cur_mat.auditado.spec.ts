import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';

test.describe('Cursos y Materias ADMIN', () => {
  test('cur 1.1 Ver paralelos de un curso (auditado)', async ({ page }) => {
    await loginAsAdmin(page);
    await page.getByRole('link', { name: 'Cursos' }).click();
    await expect(page.getByTestId('cursos-heading')).toBeVisible();

    const ver = page.getByRole('link', { name: 'Ver paralelos' }).first();
    await expect(ver).toBeVisible({ timeout: 10_000 });
    await ver.click();

    await expect(page).toHaveURL(/\/paralelos/);
    await expect(page.getByRole('heading', { level: 2, name: /Paralelos de/ })).toBeVisible();
  });

  test('mat 1.1 Alta Materia y abre detalle (auditado)', async ({ page }) => {
    const nombre = `E2E Materia ${Date.now()}`;
    await loginAsAdmin(page);
    await page.getByRole('link', { name: 'Materias' }).click();
    await page.getByRole('link', { name: '+ Nueva Materia' }).click();

    await page.getByLabel('Nombre').fill(nombre);
    await page.getByRole('button', { name: 'Crear Materia' }).click();

    await expect(page.getByRole('heading', { name: nombre })).toBeVisible({ timeout: 10_000 });
    await expect(page.getByRole('link', { name: '← Volver a Materias' })).toBeVisible();
  });
});
