// El mismo contrato del Lab1, ahora visto desde el navegador: la interfaz consume
// POST /api/chat. Este test intercepta la respuesta real y verifica su FORMA.
// Es la union entre la capa de contrato (viernes) y la capa E2E (hoy).
import { test, expect } from '@playwright/test';

test('la respuesta de /api/chat que consume la interfaz tiene la forma prometida', async ({ page }) => {
  await page.goto('/');
  const esperaRespuesta = page.waitForResponse(r => r.url().endsWith('/api/chat') && r.status() === 200);
  await page.getByLabel('Tu consulta').fill('cuantos dias tengo para devolver un producto');
  await page.getByRole('button', { name: 'Enviar' }).click();

  const cuerpo = await (await esperaRespuesta).json();
  for (const clave of ['respuesta', 'citas', 'intencion', 'urgencia', 'escalado', 'ticket', 'bloqueado', 'traza', 'thread_id']) {
    expect(cuerpo, `falta la clave ${clave}`).toHaveProperty(clave);
  }
  expect(['politica', 'pedido', 'saludo', 'otro']).toContain(cuerpo.intencion);
  expect(cuerpo.citas).toEqual(['politica_devoluciones.md']);
  await expect(page.getByTestId('citas')).toHaveText('politica_devoluciones.md');
});
