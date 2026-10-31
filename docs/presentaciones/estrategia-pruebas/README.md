# Estrategia de Pruebas y Validación — presentación

Deck de 7 slides (React 18 + Tailwind CSS 4 + Framer Motion 11), formato 16:9.
Las cifras salen de corridas reales de EduSync y están centralizadas en `src/data/metricas.js`
(cada una con su fuente). Para actualizar un número, cámbialo solo ahí.

## Usar

```powershell
npm install
npm run dev            # http://localhost:5173
npm run build          # dist/ (sitio estático)
npm run build:single   # dist-single/index.html: un solo archivo autocontenido
```

`presentacion.html` ya es la versión de un solo archivo: se abre con doble clic, sin instalar nada
(necesita internet solo para las fuentes).

## Controles

- Flechas, Re Pág / Av Pág o espacio: siguiente / anterior
- Inicio / Fin: primer / último slide
- F: pantalla completa
- `#3` en la URL abre el slide 3
- En pantallas de menos de 768 px los slides se apilan en vertical

## Estructura

```
src/
  App.jsx                 escenario 1280×720 escalado, navegación y modo móvil
  data/metricas.js        cifras y fuentes
  components/ui.jsx       SlideFrame, Tarjeta, Estado, Dato, Codigo
  slides/Slide01Titulo.jsx … Slide07Conclusiones.jsx   un componente por slide
  index.css               tokens de color y tipografía (@theme de Tailwind)
```
