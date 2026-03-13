import { Component, effect, inject, input, output, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { InputComponent } from '../../../shared/ui/input/input.component';
import { LabelComponent } from '../../../shared/ui/label/label.component';
import { CardComponent } from '../../../shared/ui/card/card.component';
import { CardHeaderComponent } from '../../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../../shared/ui/card/card-title/card-title.component';
import { CardContentComponent } from '../../../shared/ui/card/card-content/card-content.component';
import type { Group } from '../admin-page.models';

@Component({
  selector: 'app-group-form',
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
  templateUrl: './group-form.component.html',
  styleUrl: './group-form.component.less',
})
export class GroupFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly initialValue = input<Group | null>(null);
  readonly languages = input<{ name?: string }[]>([]);
  readonly save = output<{ name: string; language: string }>();
  readonly cancel = output<void>();

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(1)]],
    language: ['', Validators.required],
  });

  constructor() {
    effect(() => {
      const v = this.initialValue();
      if (v) {
        this.form.setValue({ name: v.name, language: v.language });
      } else {
        this.form.reset({ name: '', language: '' });
      }
    });
  }

  get title(): string {
    return this.initialValue() ? 'Редактировать группу' : 'Новая группа';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.save.emit(this.form.getRawValue());
  }

  onCancel(): void {
    this.cancel.emit();
  }

  getErrorMessage(controlName: string): string {
    const c = this.form.get(controlName);
    if (!c?.errors) return '';
    if (c.errors['required']) return 'Обязательное поле';
    if (c.errors['minlength']) return 'Минимум 1 символ';
    return 'Неверное значение';
  }
}
