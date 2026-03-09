import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { StudentFormComponent, type StudentFormValue } from '../student-form/student-form.component';
import type { Student, Group } from '../admin-page.models';
import { UserControllerService } from '../../../api/api/userController.service';
import { GroupControllerService } from '../../../api/api/groupController.service';
import type { UserDTO } from '../../../api/model/userDTO';
import type { GroupDTO } from '../../../api/model/groupDTO';
import { catchError, EMPTY, finalize, forkJoin } from 'rxjs';

type GroupApi = GroupDTO & { id?: number };

function toStudent(item: UserDTO): Student {
  const fullName = [item.firstName, item.lastName].filter(Boolean).join(' ') || '—';
  const groupName = item.groups?.[0]?.name ?? '—';
  return {
    id: item.id ?? 0,
    fullName,
    email: item.email ?? '',
    groupName,
  };
}

function toGroup(item: GroupApi): Group {
  return {
    id: item.id ?? 0,
    name: item.name ?? '',
    language: item.language?.name ?? '',
    teacherName: '—',
    studentsCount: '—',
  };
}

@Component({
  selector: 'app-students-tab',
  standalone: true,
  imports: [ButtonComponent, StudentFormComponent],
  templateUrl: './students-tab.component.html',
  styleUrl: './students-tab.component.less',
})
export class StudentsTabComponent implements OnInit {
  private readonly userApi = inject(UserControllerService);
  private readonly groupApi = inject(GroupControllerService);

  readonly students = signal<Student[]>([]);
  readonly groups = signal<Group[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingStudent = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.students().find(s => s.id === id) ?? null;
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      students: this.userApi.getAllStudents(),
      groups: this.groupApi.getGroups(),
    })
      .pipe(
        finalize(() => this.loading.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка загрузки данных');
          return EMPTY;
        })
      )
      .subscribe({
        next: ({ students, groups }) => {
          this.students.set(students.map(toStudent));
          this.groups.set((groups as GroupApi[]).map(toGroup));
        },
      });
  }

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(s: Student): void {
    this.editingId.set(s.id);
    this.showForm.set(true);
  }

  onSave(value: StudentFormValue): void {
    const id = this.editingId();
    this.saving.set(true);
    this.error.set(null);

    const done = () => {
      this.saving.set(false);
      this.showForm.set(false);
      this.editingId.set(null);
      this.loadData();
    };

    if (id !== null) {
      this.userApi.updateStudent(id, {
        firstName: value.firstName,
        lastName: value.lastName,
        grade: value.grade,
      })
        .pipe(
          finalize(() => this.saving.set(false)),
          catchError(err => {
            this.error.set(err?.message ?? 'Ошибка сохранения');
            return EMPTY;
          })
        )
        .subscribe({ next: () => done() });
    } else {
      const groupIds = value.groupId != null ? [value.groupId] : [];
      this.userApi.createStudent({
        firstName: value.firstName,
        lastName: value.lastName,
        email: value.email,
        password: value.password ?? '',
        groupIds,
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

  deleteStudent(id: number): void {
    this.saving.set(true);
    this.error.set(null);
    this.userApi.deleteStudent(id)
      .pipe(
        finalize(() => this.saving.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка удаления');
          return EMPTY;
        })
      )
      .subscribe({ next: () => this.loadData() });
  }
}
