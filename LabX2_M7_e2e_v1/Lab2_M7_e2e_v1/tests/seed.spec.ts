// Archivo SEMILLA para los agentes Planner y Generator.
// Los agentes lo usan para saber como se abre la app y que fixtures existen.
// No prueba nada: es el punto de partida que se les entrega.
import { test, expect } from '@playwright/test';

test.describe('SoporteIA', () => {
  test('semilla', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'SoporteIA' })).toBeVisible();
  });
});
