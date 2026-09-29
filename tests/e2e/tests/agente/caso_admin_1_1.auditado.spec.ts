// Generado/auditado — caso 1.1 plan ADMIN (localizadores robustos).
import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';

test.describe('Login ADMIN', () => {
  test('1.1 Camino feliz ADMIN (auditado)', async ({ page }) => {
    await loginAsAdmin(page);

    await expect(page).toHaveURL(/\/usuarios/);
    await expect(page.getByTestId('usuarios-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Tenants' })).toHaveCount(0);
  });
});
