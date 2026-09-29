import { test, expect } from '@playwright/test';
import { sysadminCreds } from '../helpers/auth';

test.describe('Login EduSync', () => {
  test('camino feliz SYSADMIN', async ({ page }) => {
    await page.goto('/login');

    const { email, password } = sysadminCreds();
    await page.getByLabel('Correo electrónico').fill(email);
    await page.getByLabel('Contraseña').fill(password);
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();

    await expect(page).toHaveURL(/\/plataforma\/tenants/);
    await expect(page.getByTestId('tenants-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Tenants' })).toBeVisible();
  });
});
