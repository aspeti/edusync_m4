// GENERADO por el agente Generator a partir de specs/plan_chat_soporteia.md (1.1), tal como salio.
// AUDITORIA: ACEPTADO. Localizadores por rol/etiqueta/testid, sin esperas fijas, un solo test.
// Es identico en espiritu al que escribio el docente: el agente imito bien el patron del seed
// y del archivo a mano que se le dio como contexto.
import { test, expect } from '@playwright/test';

test.describe('Envío de consultas', () => {
  test('Saludo', async ({ page }) => {
    // 1. Abrir /
    await page.goto('/');
    // 2. Escribir «hola» en «Tu consulta»
    await page.getByLabel('Tu consulta').fill('hola');
    // 3. Pulsar «Enviar»
    await page.getByRole('button', { name: 'Enviar' }).click();
    // Resultado esperado
    await expect(page.getByTestId('mensaje-asistente')).toBeVisible();
    await expect(page.getByTestId('intencion')).toHaveText('saludo');
    await expect(page.getByRole('status')).toHaveText('Listo');
    await expect(page.getByTestId('traza').getByRole('listitem')).toHaveCount(4);
  });
});
