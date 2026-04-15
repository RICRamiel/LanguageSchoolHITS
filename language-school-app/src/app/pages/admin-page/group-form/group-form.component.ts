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

export interface GroupFormValue {
  name: string;
  teacherId: string;
  languageId: string;
  description: string;
  satisfactoryThreshold: number;
  goodThreshold: number;
  excellentThreshold: number;
}

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
  readonly languages = input<{ id?: string; name?: string }[]>([]);
  readonly teachers = input<{ id?: string; fullName?: string }[]>([]);
  readonly save = output<GroupFormValue>();
  readonly cancel = output<void>();

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(1)]],
    teacherId: [''],
    languageId: [''],
    description: ['', [Validators.required, Validators.minLength(1)]],
    satisfactoryThreshold: [60, [Validators.required, Validators.min(0), Validators.max(100)]],
    goodThreshold: [75, [Validators.required, Validators.min(0), Validators.max(100)]],
    excellentThreshold: [90, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  constructor() {
    effect(() => {
      const v = this.initialValue();
      if (v) {
        this.form.patchValue({ name: v.name, teacherId: '', languageId: '' });
        this.form.get('teacherId')?.clearValidators();
        this.form.get('languageId')?.clearValidators();
        this.form.get('description')?.clearValidators();
        this.form.get('satisfactoryThreshold')?.clearValidators();
        this.form.get('goodThreshold')?.clearValidators();
        this.form.get('excellentThreshold')?.clearValidators();
      } else {
        this.form.reset({
          name: '',
          teacherId: '',
          languageId: '',
          description: '',
          satisfactoryThreshold: 60,
          goodThreshold: 75,
          excellentThreshold: 90,
        });
        this.form.get('teacherId')?.setValidators(Validators.required);
        this.form.get('languageId')?.setValidators(Validators.required);
        this.form.get('description')?.setValidators([Validators.required, Validators.minLength(1)]);
        this.form.get('satisfactoryThreshold')?.setValidators([Validators.required, Validators.min(0), Validators.max(100)]);
        this.form.get('goodThreshold')?.setValidators([Validators.required, Validators.min(0), Validators.max(100)]);
        this.form.get('excellentThreshold')?.setValidators([Validators.required, Validators.min(0), Validators.max(100)]);
      }
      ['teacherId', 'languageId', 'description', 'satisfactoryThreshold', 'goodThreshold', 'excellentThreshold'].forEach(
        (f) => this.form.get(f)?.updateValueAndValidity(),
      );
    });
  }

  get title(): string {
    return this.initialValue() ? 'Редактировать курс' : 'Новый курс';
  }

  get isEdit(): boolean {
    return !!this.initialValue();
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
    if (c.errors['min']) return 'Минимум 0';
    if (c.errors['max']) return 'Максимум 100';
    return 'Неверное значение';
  }
}
