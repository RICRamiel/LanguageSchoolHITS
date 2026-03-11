import { Component, computed, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { TeacherFormComponent } from '../teacher-form/teacher-form.component';
import type { Teacher } from '../admin-page.models';
import { UserControllerService } from '../../../api/api/userController.service';
import type { UserDTO } from '../../../api/model/userDTO';
import { catchError, EMPTY, finalize } from 'rxjs';

function toTeacher(item: UserDTO): Teacher {
  const fullName = [item.firstName, item.lastName].filter(Boolean).join(' ') || '—';
  return {
    id: item.id ?? 0,
    fullName,
    email: item.email ?? '',
    languages: '—',
  };
}

@Component({
  selector: 'app-teachers-tab',
  standalone: true,
  imports: [ButtonComponent, TeacherFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teachers-tab.component.html',
  styleUrl: './teachers-tab.component.less',
})
export class TeachersTabComponent implements OnInit {
  private readonly userApi = inject(UserControllerService);

  readonly teachers = signal<Teacher[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingTeacher = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.teachers().find(t => t.id === id) ?? null;
  });

  ngOnInit(): void {
    this.loadTeachers();
  }

  loadTeachers(): void {
    this.loading.set(true);
    this.error.set(null);
    this.userApi.getAllTeachers()
      .pipe(
        finalize(() => this.loading.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка загрузки преподавателей');
          return EMPTY;
        })
      )
      .subscribe({
        next: items => this.teachers.set(items.map(toTeacher)),
      });
  }

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(t: Teacher): void {
    this.editingId.set(t.id);
    this.showForm.set(true);
  }

  onSave(value: { firstName: string; lastName: string; email: string; password?: string }): void {
    const id = this.editingId();
    this.saving.set(true);
    this.error.set(null);

    const done = () => {
      this.saving.set(false);
      this.showForm.set(false);
      this.editingId.set(null);
      this.loadTeachers();
    };

    if (id !== null) {
      this.userApi.updateTeacher(id, { firstName: value.firstName, lastName: value.lastName })
        .pipe(
          finalize(() => this.saving.set(false)),
          catchError(err => {
            this.error.set(err?.message ?? 'Ошибка сохранения');
            return EMPTY;
          })
        )
        .subscribe({ next: () => done() });
    } else {
      this.userApi.createTeacher({
        firstName: value.firstName,
        lastName: value.lastName,
        email: value.email,
        password: value.password ?? '',
      })
        .pipe(
          finalize(() => this.saving.set(false)),
          catchError(err => {
            this.error.set(err?.message ?? 'Ошибка создания');
            return EMPTY;
          })
        )
        .subscribe({ next: () => done() });
    }
  }

  onCancel(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  deleteTeacher(id: number): void {
    this.saving.set(true);
    this.error.set(null);
    this.userApi.deleteTeacher(id)
      .pipe(
        finalize(() => this.saving.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка удаления');
          return EMPTY;
        })
      )
      .subscribe({ next: () => this.loadTeachers() });
  }
}
