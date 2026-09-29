import { test, expect } from '@playwright/test';

test.describe('Auth pública', () => {
  test('auth 2.1 Pantalla restablecer contraseña (auditado)', async ({ page }) => {
    await page.goto('/restablecer-password');

    await expect(page.getByRole('heading', { name: 'Restablecer contraseña' })).toBeVisible();
    await expect(page.getByLabel('Token')).toBeVisible();
    await expect(page.getByLabel('Nueva contraseña')).toBeVisible();
  });
});
