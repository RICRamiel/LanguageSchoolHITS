import { Component, effect, inject, input, output, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { InputComponent } from '../../../shared/ui/input/input.component';
import { LabelComponent } from '../../../shared/ui/label/label.component';
import { CardComponent } from '../../../shared/ui/card/card.component';
import { CardHeaderComponent } from '../../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../../shared/ui/card/card-title/card-title.component';
import { CardContentComponent } from '../../../shared/ui/card/card-content/card-content.component';
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
  readonly save = output<{ firstName: string; lastName: string; email: string; password?: string }>();
  readonly cancel = output<void>();

  form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
  });

  constructor() {
    effect(() => {
      const v = this.initialValue();
      if (v) {
        const parts = v.fullName.trim().split(/\s+/);
        const lastName = parts[0] ?? '';
        const firstName = parts.slice(1).join(' ') ?? '';
        this.form.setValue({ firstName, lastName, email: v.email, password: '' });
        this.form.get('email')?.disable();
        this.form.get('password')?.disable();
      } else {
        this.form.reset({ firstName: '', lastName: '', email: '', password: '' });
        this.form.get('email')?.enable();
        this.form.get('password')?.enable();
      }
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
    if (this.isEdit) {
      this.save.emit({ firstName: raw.firstName, lastName: raw.lastName, email: raw.email });
    } else {
      if (!raw.password?.trim()) {
        this.form.get('password')?.setErrors({ required: true });
        this.form.markAllAsTouched();
        return;
      }
      this.save.emit({ ...raw, password: raw.password });
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
