import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';

test.describe('Gestión Escolar ADMIN', () => {
  test('ges 1.1 Alta Gestión Escolar (auditado)', async ({ page }) => {
    const nombre = `E2E Gestión ${Date.now()}`;
    const y = new Date().getFullYear() + 1;
    await loginAsAdmin(page);
    await page.getByRole('link', { name: 'Gestión Escolar' }).click();
    await page.getByRole('link', { name: '+ Nueva Gestión Escolar' }).click();

    await page.getByLabel('Nombre').fill(nombre);
    await page.getByLabel('Fecha de inicio').fill(`${y}-02-01`);
    await page.getByLabel('Fecha de fin').fill(`${y}-11-30`);
    await page.getByRole('button', { name: 'Crear Gestión Escolar' }).click();

    await expect(page).toHaveURL(/\/academico\/gestiones-escolares/);
    await expect(page.getByTestId('gestiones-heading')).toBeVisible();
    await expect(page.getByText(nombre)).toBeVisible({ timeout: 10_000 });
  });

  test('ges 1.2 Abrir Periodos de una gestión (auditado)', async ({ page }) => {
    await loginAsAdmin(page);
    await page.getByRole('link', { name: 'Gestión Escolar' }).click();
    await expect(page.getByTestId('gestiones-heading')).toBeVisible();

    const periodos = page.getByRole('link', { name: 'Periodos' }).first();
    await expect(periodos).toBeVisible({ timeout: 10_000 });
    await periodos.click();

    await expect(page).toHaveURL(/\/periodos/);
    await expect(page.getByRole('heading', { name: /Periodos/ })).toBeVisible();
  });

  test('ges 1.3 Abrir Secciones de una gestión (auditado)', async ({ page }) => {
    await loginAsAdmin(page);
    await page.getByRole('link', { name: 'Gestión Escolar' }).click();

    const secciones = page.getByRole('link', { name: 'Secciones' }).first();
    await expect(secciones).toBeVisible({ timeout: 10_000 });
    await secciones.click();

    await expect(page).toHaveURL(/\/secciones/);
    await expect(page.getByRole('heading', { name: /Secciones/ })).toBeVisible();
  });
});
