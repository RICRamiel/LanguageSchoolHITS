import { Component, effect, inject, input, output, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormsModule } from '@angular/forms';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { InputComponent } from '../../../shared/ui/input/input.component';
import { LabelComponent } from '../../../shared/ui/label/label.component';
import { CardComponent } from '../../../shared/ui/card/card.component';
import { CardHeaderComponent } from '../../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../../shared/ui/card/card-title/card-title.component';
import { CardContentComponent } from '../../../shared/ui/card/card-content/card-content.component';
import type { Student } from '../admin-page.models';
import type { Group } from '../admin-page.models';

export interface StudentFormValue {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  groupId: string | null;
}

@Component({
  selector: 'app-student-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    ButtonComponent,
    InputComponent,
    LabelComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './student-form.component.html',
  styleUrl: './student-form.component.less',
})
export class StudentFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly initialValue = input<Student | null>(null);
  readonly groups = input<Group[]>([]);
  readonly save = output<StudentFormValue>();
  readonly cancel = output<void>();

  form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(1)]],
    lastName: ['', [Validators.required, Validators.minLength(1)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8), Validators.maxLength(64)]],
    groupId: [null as string | null],
  });

  constructor() {
    effect(() => {
      const v = this.initialValue();
      const grps = this.groups();
      const pwdCtrl = this.form.get('password');
      const grpCtrl = this.form.get('groupId');
      if (v) {
        const parts = v.fullName.trim().split(/\s+/);
        const lastName = parts[0] ?? '';
        const firstName = parts.slice(1).join(' ') ?? '';
        const groupId = v.groupIds?.[0] ?? grps.find((g) => g.name === v.groupName)?.id ?? null;
        this.form.setValue({
          firstName,
          lastName,
          email: v.email,
          password: '',
          groupId,
        });
        this.form.get('email')?.disable();
        pwdCtrl?.clearValidators();
        pwdCtrl?.disable();
        grpCtrl?.clearValidators();
        grpCtrl?.disable();
      } else {
        this.form.reset({
          firstName: '',
          lastName: '',
          email: '',
          password: '',
          groupId: null,
        });
        this.form.get('email')?.enable();
        pwdCtrl?.setValidators([Validators.required, Validators.minLength(8), Validators.maxLength(64)]);
        pwdCtrl?.enable();
        grpCtrl?.clearValidators();
        grpCtrl?.enable();
      }
      pwdCtrl?.updateValueAndValidity();
      grpCtrl?.updateValueAndValidity();
    });
  }

  get title(): string {
    return this.initialValue() ? 'Редактировать студента' : 'Новый студент';
  }

  get isEdit(): boolean {
    return !!this.initialValue();
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
      groupId: raw.groupId,
    });
  }

  onCancel(): void {
    this.cancel.emit();
  }

  getErrorMessage(controlName: string): string {
    const c = this.form.get(controlName);
    if (!c?.errors) return '';
    if (c.errors['required']) return 'Обязательное поле';
    if (c.errors['email']) return 'Неверный формат почты';
    if (c.errors['minlength']) return `Минимум ${c.errors['minlength'].requiredLength} символов`;
    if (c.errors['maxlength']) return `Максимум ${c.errors['maxlength'].requiredLength} символов`;
    return 'Неверное значение';
  }
}
