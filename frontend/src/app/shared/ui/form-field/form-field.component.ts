import { Component, EventEmitter, Input, Output } from '@angular/core';

let nextId = 0;

/**
 * Campo de formulario del Design System EduSync (ADR-0015): microetiqueta
 * en mayúsculas (transformada vía CSS, no en el dato), input nativo e
 * ícono decorativo opcional a la derecha (proyectado por el consumidor
 * como SVG, vía `[app-form-field-icon]`).
 *
 * Expone `[(value)]` (two-way binding simple mediante `@Input()`/`@Output()`)
 * en lugar de implementar `ControlValueAccessor`, para poder reemplazar los
 * `[(ngModel)]` de `login.page.ts` sin alterar su lógica de envío
 * (DD-UC-020 §2, PR-IMPL-020 §1.4 paso 6: "el FormGroup/ngModel... el
 * método onSubmit()... permanecen idénticos").
 *
 * Primer consumidor: DD-UC-020 (login), vía
 * frontend/src/app/features/auth/login/login.page.ts.
 */
@Component({
  selector: 'app-form-field',
  standalone: true,
  template: `
    <div class="app-form-field">
      <label [for]="fieldId" class="app-form-field__label">{{ label }}</label>
      <div class="app-form-field__control">
        <input
          [id]="fieldId"
          [type]="type"
          [name]="name || fieldId"
          [value]="value"
          [attr.autocomplete]="autocomplete"
          [attr.placeholder]="placeholder"
          [required]="required"
          (input)="onInput($event)"
          class="app-form-field__input"
        />
        <span class="app-form-field__icon" aria-hidden="true">
          <ng-content select="[app-form-field-icon]"></ng-content>
        </span>
      </div>
    </div>
  `,
  styles: [
    `
      .app-form-field {
        margin-bottom: var(--space-md);
        text-align: left;
      }

      .app-form-field__label {
        display: block;
        font-size: var(--font-size-label);
        font-weight: var(--font-weight-label);
        letter-spacing: var(--letter-spacing-label);
        text-transform: uppercase;
        color: var(--color-text-secondary);
        margin-bottom: var(--space-xs);
      }

      .app-form-field__control {
        position: relative;
      }

      .app-form-field__input {
        width: 100%;
        box-sizing: border-box;
        padding: var(--space-sm) var(--space-md);
        padding-right: 2.25rem;
        font-size: var(--font-size-body);
        font-family: var(--font-family-base);
        color: var(--color-text);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }

      .app-form-field__input:focus {
        outline: none;
        border-color: var(--color-primary);
        box-shadow: 0 0 0 3px var(--color-accent);
      }

      .app-form-field__icon {
        position: absolute;
        right: var(--space-md);
        top: 50%;
        transform: translateY(-50%);
        color: var(--color-text-secondary);
        display: flex;
        pointer-events: none;
      }

      .app-form-field__icon:empty {
        display: none;
      }
    `,
  ],
})
export class FormFieldComponent {
  @Input() label = '';
  @Input() type: 'text' | 'email' | 'password' = 'text';
  @Input() name?: string;
  @Input() autocomplete?: string;
  @Input() placeholder?: string;
  @Input() required = false;
  @Input() value = '';
  @Output() valueChange = new EventEmitter<string>();

  readonly fieldId = `app-form-field-${nextId++}`;

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.value = target.value;
    this.valueChange.emit(this.value);
  }
}
