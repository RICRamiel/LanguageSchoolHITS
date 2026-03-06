import { Component, effect, inject, input, output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { InputComponent } from '../../../shared/ui/input/input.component';
import { LabelComponent } from '../../../shared/ui/label/label.component';
import { CardComponent } from '../../../shared/ui/card/card.component';
import { CardHeaderComponent } from '../../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../../shared/ui/card/card-title/card-title.component';
import { CardContentComponent } from '../../../shared/ui/card/card-content/card-content.component';
import type { Student } from '../admin-page.models';

@Component({
  selector: 'app-student-form',
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
  templateUrl: './student-form.component.html',
  styleUrl: './student-form.component.less',
})
export class StudentFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly initialValue = input<Student | null>(null);
  readonly save = output<Omit<Student, 'id'>>();
  readonly cancel = output<void>();

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    groupName: ['', Validators.required],
  });

  constructor() {
    effect(() => {
      const v = this.initialValue();
      if (v) {
        this.form.setValue({ fullName: v.fullName, email: v.email, groupName: v.groupName });
      } else {
        this.form.reset({ fullName: '', email: '', groupName: '' });
      }
    });
  }

  get title(): string {
    return this.initialValue() ? 'Редактировать студента' : 'Новый студент';
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
}
