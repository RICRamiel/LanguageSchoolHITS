import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { CardComponent } from '../../../shared/ui/card/card.component';
import { CardContentComponent } from '../../../shared/ui/card/card-content/card-content.component';
import { CardHeaderComponent } from '../../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../../shared/ui/card/card-title/card-title.component';
import { InputComponent } from '../../../shared/ui/input/input.component';
import { LabelComponent } from '../../../shared/ui/label/label.component';
import type { Teacher } from '../admin-page.models';

@Component({
  selector: 'app-teacher-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonComponent,
    InputComponent,
    LabelComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-form.component.html',
  styleUrl: './teacher-form.component.less',
})
export class TeacherFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly initialValue = input<Teacher | null>(null);
  readonly save = output<{
    firstName: string;
    lastName: string;
    email: string;
    password?: string;
  }>();
  readonly cancel = output<void>();

  form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(1)]],
    lastName: ['', [Validators.required, Validators.minLength(1)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8), Validators.maxLength(64)]],
  });

  constructor() {
    effect(() => {
      const value = this.initialValue();
      const passwordControl = this.form.get('password');

      if (value) {
        const parts = value.fullName.trim().split(/\s+/);
        const lastName = parts[0] ?? '';
        const firstName = parts.slice(1).join(' ') ?? '';

        this.form.setValue({
          firstName,
          lastName,
          email: value.email,
          password: '',
        });
        this.form.get('email')?.disable();
        passwordControl?.clearValidators();
        passwordControl?.disable();
      } else {
        this.form.reset({ firstName: '', lastName: '', email: '', password: '' });
        this.form.get('email')?.enable();
        passwordControl?.setValidators([
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(64),
        ]);
        passwordControl?.enable();
      }

      passwordControl?.updateValueAndValidity();
    });
  }

  get isEdit(): boolean {
    return !!this.initialValue();
  }

  get title(): string {
    return this.initialValue() ? 'Редактировать преподавателя' : 'Новый преподаватель';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.save.emit({
      firstName: raw.firstName,
      lastName: raw.lastName,
      email: raw.email,
      password: this.isEdit ? undefined : raw.password,
    });
  }

  onCancel(): void {
    this.cancel.emit();
  }

  getErrorMessage(controlName: string): string {
    const control = this.form.get(controlName);
    if (!control?.errors) return '';
    if (control.errors['required']) return 'Обязательное поле';
    if (control.errors['email']) return 'Неверный формат почты';
    if (control.errors['minlength']) return `Минимум ${control.errors['minlength'].requiredLength} символов`;
    if (control.errors['maxlength']) return `Максимум ${control.errors['maxlength'].requiredLength} символов`;
    return 'Неверное значение';
  }
}
