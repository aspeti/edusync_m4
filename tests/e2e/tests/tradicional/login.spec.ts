// Tests E2E escritos A MANO. Patrón que el Generator debe imitar.
//
// Reglas:
//   1. Localizadores por rol, etiqueta o data-testid — nunca CSS / nth-child.
//   2. Sin esperas fijas (nada de waitForTimeout).
//   3. Cada test empieza limpio (goto /login) y no depende de otro.
//
// Prerrequisito: frontend :4200 + backend :8080 + seed SYSADMIN.
import { test, expect } from '@playwright/test';
import { loginAs, loginAsSysAdmin, sysadminCreds } from '../helpers/auth';

test.describe('Login EduSync', () => {
  test('la pantalla de login muestra marca y formulario', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByText('EduSync').first()).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Bienvenido de nuevo' })).toBeVisible();
    await expect(page.getByLabel('Correo electrónico')).toBeVisible();
    await expect(page.getByLabel('Contraseña')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Iniciar sesión' })).toBeVisible();
  });

  test('credenciales inválidas muestran alerta y no navegan', async ({ page }) => {
    await loginAs(page, 'nobody@edusync.local', 'wrong-password');

    await expect(page.getByTestId('login-error')).toContainText('Credenciales inválidas');
    await expect(page).toHaveURL(/\/login/);
  });

  test('SYSADMIN autentica y llega a la consola de Tenants', async ({ page }) => {
    await loginAsSysAdmin(page);

    await expect(page).toHaveURL(/\/plataforma\/tenants/);
    await expect(page.getByRole('link', { name: 'Tenants' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Cerrar sesión' })).toBeVisible();
  });

  test('Cerrar sesión vuelve al login', async ({ page }) => {
    await loginAsSysAdmin(page);

    await page.getByRole('button', { name: 'Cerrar sesión' }).click();

    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole('heading', { name: 'Bienvenido de nuevo' })).toBeVisible();
  });
});

test.describe('Consola SysAdmin — Tenants', () => {
  test('tras login SYSADMIN la lista de tenants es visible', async ({ page }) => {
    const { email } = sysadminCreds();
    test.info().annotations.push({ type: 'actor', description: `SYSADMIN ${email}` });

    await loginAsSysAdmin(page);

    await expect(page.getByTestId('tenants-heading')).toHaveText('Tenants');
    await expect(page.getByRole('link', { name: '+ Nuevo Tenant' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Buscar' })).toBeVisible();
  });
});
