import { test, expect } from '@playwright/test';

test.describe('Login EduSync', () => {
  test('credenciales inválidas muestran alerta y no navegan', async ({ page }) => {
    // Abrir /login
    await page.goto('/login');

    // correo inventado + contraseña incorrecta
    await page.getByLabel('Correo electrónico').fill('nobody@edusync.local');
    await page.getByLabel('Contraseña').fill('wrong-password');

    // Iniciar sesión
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();

    // alerta login-error contiene «Credenciales inválidas»
    await expect(page.getByTestId('login-error')).toContainText('Credenciales inválidas');

    // URL sigue en /login
    await expect(page).toHaveURL(/\/login/);
  });
});
