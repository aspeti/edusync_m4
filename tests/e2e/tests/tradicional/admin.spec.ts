// Flujos E2E del ADMIN de tenant (usuario demo).
// Reglas Lab M7: getByRole/Label/TestId; sin waitForTimeout; cada test independiente.
import { test, expect } from '@playwright/test';
import { adminCreds, loginAsAdmin } from '../helpers/auth';

test.describe('Login ADMIN demo', () => {
  test('autentica y redirige a Usuarios', async ({ page }) => {
    await loginAsAdmin(page);

    await expect(page).toHaveURL(/\/usuarios/);
    await expect(page.getByTestId('usuarios-heading')).toHaveText('Usuarios');
    await expect(page.getByRole('link', { name: 'Tenants' })).toHaveCount(0);
  });

  test('nav ADMIN muestra consolas académicas', async ({ page }) => {
    await loginAsAdmin(page);

    await expect(page.getByRole('link', { name: 'Usuarios' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Cursos' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Gestión Escolar' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Materias' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Estudiantes' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Profesores' })).toBeVisible();
  });
});

test.describe('Consola ADMIN — navegación de listas', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test('Usuarios: lista y alta disponibles', async ({ page }) => {
    await expect(page.getByTestId('usuarios-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: '+ Nuevo Usuario' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Buscar' })).toBeVisible();
  });

  test('Gestión Escolar: lista y alta', async ({ page }) => {
    await page.getByRole('link', { name: 'Gestión Escolar' }).click();

    await expect(page).toHaveURL(/\/academico\/gestiones-escolares/);
    await expect(page.getByTestId('gestiones-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: '+ Nueva Gestión Escolar' })).toBeVisible();
  });

  test('Cursos: lista y alta', async ({ page }) => {
    await page.getByRole('link', { name: 'Cursos' }).click();

    await expect(page).toHaveURL(/\/academico\/cursos/);
    await expect(page.getByTestId('cursos-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: '+ Nuevo Curso' })).toBeVisible();
  });

  test('Materias: lista y alta', async ({ page }) => {
    await page.getByRole('link', { name: 'Materias' }).click();

    await expect(page).toHaveURL(/\/academico\/materias/);
    await expect(page.getByTestId('materias-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: '+ Nueva Materia' })).toBeVisible();
  });

  test('Estudiantes: lista y alta', async ({ page }) => {
    await page.getByRole('link', { name: 'Estudiantes' }).click();

    await expect(page).toHaveURL(/\/academico\/estudiantes/);
    await expect(page.getByTestId('estudiantes-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: '+ Nuevo Estudiante' })).toBeVisible();
  });

  test('Profesores: lista de solo lectura (alta vía Usuarios)', async ({ page }) => {
    await page.getByRole('link', { name: 'Profesores' }).click();

    await expect(page).toHaveURL(/\/academico\/profesores/);
    await expect(page.getByTestId('profesores-heading')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Crear usuario' })).toBeVisible();
  });
});

test.describe('Consola ADMIN — escrituras con datos propios', () => {
  test('crea un Curso con nombre único y vuelve a la lista', async ({ page }) => {
    const nombre = `E2E Curso ${Date.now()}`;
    await loginAsAdmin(page);

    await page.getByRole('link', { name: 'Cursos' }).click();
    await page.getByRole('link', { name: '+ Nuevo Curso' }).click();
    await expect(page.getByRole('heading', { name: 'Nuevo Curso' })).toBeVisible();

    await page.getByLabel('Nombre').fill(nombre);
    await page.getByRole('button', { name: 'Crear Curso' }).click();

    await expect(page).toHaveURL(/\/academico\/cursos/);
    await expect(page.getByTestId('cursos-heading')).toBeVisible();
    // Buscar por nombre: la lista está paginada
    await page.getByPlaceholder('Buscar por nombre...').fill(nombre);
    await page.getByRole('button', { name: 'Buscar' }).click();
    await expect(page.getByText(nombre)).toBeVisible({ timeout: 10_000 });
  });

  test('crea un Estudiante con RUDE único y abre su detalle', async ({ page }) => {
    const suffix = String(Date.now()).slice(-10);
    const rude = `E2E${suffix}`.slice(0, 20);
    const nombre = `E2E Estudiante ${suffix}`;
    await loginAsAdmin(page);

    await page.getByRole('link', { name: 'Estudiantes' }).click();
    await page.getByRole('link', { name: '+ Nuevo Estudiante' }).click();
    await expect(page.getByRole('heading', { name: 'Nuevo Estudiante' })).toBeVisible();

    await page.getByLabel('RUDE').fill(rude);
    await page.getByLabel('Nombre completo').fill(nombre);
    await page.getByRole('button', { name: 'Crear Estudiante' }).click();

    // POST exitoso navega al detalle (/estudiantes/{id}), no a la lista
    await expect(page.getByRole('heading', { name: nombre })).toBeVisible({ timeout: 10_000 });
    await expect(page.getByText(new RegExp(`RUDE:\\s*${rude}`))).toBeVisible();
    await expect(page.getByRole('link', { name: '← Volver a Estudiantes' })).toBeVisible();
  });

  test('abre formulario Nuevo Usuario con roles seleccionables', async ({ page }) => {
    const { email } = adminCreds();
    test.info().annotations.push({ type: 'actor', description: `ADMIN ${email}` });

    await loginAsAdmin(page);
    await page.getByRole('link', { name: '+ Nuevo Usuario' }).click();

    await expect(page.getByRole('heading', { name: 'Nuevo Usuario' })).toBeVisible();
    await expect(page.getByLabel('Nombre completo')).toBeVisible();
    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Contraseña inicial')).toBeVisible();
    await expect(page.getByText('SECRETARIA', { exact: true })).toBeVisible();
    await expect(page.getByText('PROFESOR', { exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Crear Usuario' })).toBeDisabled();
  });
});
