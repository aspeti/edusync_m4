// GENERADO por el agente Generator a partir del plan (1.4), tal como salio.
// Esta VERDE. Y tiene tres defectos tipicos que la auditoria E2E tiene que ver:
//
//   AUDITORIA 1 (localizador fragil): page.locator('#mensajes li:nth-child(2)') depende de la
//     POSICION del mensaje y de un id de CSS. Si se agrega un mensaje de bienvenida, se rompe.
//     Correcto: page.getByTestId('mensaje-asistente') o getByRole('log').getByRole('listitem').last()
//
//   AUDITORIA 2 (espera fija): page.waitForTimeout(2000) suma dos segundos a CADA corrida y no
//     garantiza nada (si la respuesta tarda 2,1 s, falla igual). Correcto: expect(...).toContainText()
//     ya espera hasta que aparezca.
//
//   AUDITORIA 3 (asegura el texto del modelo): toHaveText con la redaccion completa. Con las reglas
//     de demo pasa; con el modelo real se pondria rojo cada vez que el modelo lo diga distinto.
//     Correcto: verificar la FUENTE (citas) y que la respuesta no esta vacia.
//
// Veredicto: CORREGIDO -> ver generado-politica.auditado.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Envío de consultas', () => {
  test('Consulta de política', async ({ page }) => {
    await page.goto('/');
    await page.fill('#pregunta', 'cuántos días tengo para devolver');
    await page.click('#enviar');
    await page.waitForTimeout(2000);
    await expect(page.locator('#mensajes li:nth-child(2)')).toHaveText(
      'Tienes 30 dias calendario desde la entrega para solicitar una devolucion; en electronicos, 14 dias con sello de fabrica. (fuente: politica_devoluciones.md)');
  });
});
