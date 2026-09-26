// Ejemplo de test estilo "generado por agente" YA AUDITADO.
// Sirve de referencia si el Generator produce CSS / waitForTimeout / copy frágil.
import { test, expect } from '@playwright/test';
import { loginAsSysAdmin } from '../helpers/auth';

test.describe('Login', () => {
  test('1.1 Camino feliz SYSADMIN (auditado)', async ({ page }) => {
    // Abrir login e ingresar como SYSADMIN del seed
    await loginAsSysAdmin(page);

    // Resultado esperado: consola Tenants
    await expect(page).toHaveURL(/\/plataforma\/tenants/);
    await expect(page.getByTestId('tenants-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Tenants' })).toBeVisible();
  });
});
