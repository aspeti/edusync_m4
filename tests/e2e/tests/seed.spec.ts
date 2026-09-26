// SEMILLA para Planner / Generator.
// Muestra cómo se abre la app y qué anclas existen. No es la suite de negocio.
import { test, expect } from '@playwright/test';

test.describe('EduSync', () => {
  test('semilla — pantalla de login visible', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: 'Bienvenido de nuevo' })).toBeVisible();
    await expect(page.getByLabel('Correo electrónico')).toBeVisible();
    await expect(page.getByLabel('Contraseña')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Iniciar sesión' })).toBeVisible();
  });
});
