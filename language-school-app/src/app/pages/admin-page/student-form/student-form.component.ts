import { Component, effect, inject, input, output } from '@angular/core';
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
  groupId: number | null;
  grade?: string;
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
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    groupId: [null as number | null, Validators.required],
    grade: [''],
  });

  constructor() {
    effect(() => {
      const v = this.initialValue();
      if (v) {
        const parts = v.fullName.trim().split(/\s+/);
        const lastName = parts[0] ?? '';
        const firstName = parts.slice(1).join(' ') ?? '';
        this.form.setValue({
          firstName,
          lastName,
          email: v.email,
          password: '',
          groupId: null,
          grade: '',
        });
        this.form.get('email')?.disable();
        this.form.get('password')?.disable();
        this.form.get('groupId')?.disable();
      } else {
        this.form.reset({
          firstName: '',
          lastName: '',
          email: '',
          password: '',
          groupId: null,
          grade: '',
        });
        this.form.get('email')?.enable();
        this.form.get('password')?.enable();
        this.form.get('groupId')?.enable();
      }
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
    if (!this.isEdit && !raw.password?.trim()) {
      this.form.get('password')?.setErrors({ required: true });
      this.form.markAllAsTouched();
      return;
    }
    this.save.emit({
      firstName: raw.firstName,
      lastName: raw.lastName,
      email: raw.email,
      password: this.isEdit ? undefined : raw.password,
      groupId: raw.groupId,
      grade: raw.grade || undefined,
    });
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
