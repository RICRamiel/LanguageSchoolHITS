import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { TeacherFormComponent } from '../teacher-form/teacher-form.component';
import { AssignGroupModalComponent } from '../assign-group-modal/assign-group-modal.component';
import type { Teacher, Group } from '../admin-page.models';
import { AdminService, type AdminGroupDTO, type AdminUserDTO } from '../../../core/admin/admin.service';
import { catchError, concatMap, EMPTY, finalize, forkJoin, map, of, switchMap } from 'rxjs';

function toTeacher(dto: AdminUserDTO, groups: AdminGroupDTO[]): Teacher {
  const first = dto.firstName ?? '';
  const last = dto.lastName ?? '';
  const fullName = [first, last].filter(Boolean).join(' ') || '—';
  const firstGroup = groups[0];
  const languages = [
    ...new Set(groups.map((g) => g.language?.name).filter((n): n is string => !!n)),
  ].join(', ');
  return {
    id: dto.id ?? 0,
    fullName,
    email: dto.email ?? '',
    languages: languages || '—',
    groupId: firstGroup?.id ?? null,
    groupName: firstGroup?.name ?? '—',
  };
}

@Component({
  selector: 'app-teachers-tab',
  standalone: true,
  imports: [ButtonComponent, TeacherFormComponent, AssignGroupModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teachers-tab.component.html',
  styleUrl: './teachers-tab.component.less',
})
export class TeachersTabComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  teachers: Teacher[] = [];
  groups: Group[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  showForm = false;
  editingId: number | null = null;
  showAssignModal = false;
  assignUserId: number | null = null;

  get editingTeacher(): Teacher | null {
    if (this.editingId === null) return null;
    return this.teachers.find((t) => t.id === this.editingId!) ?? null;
  }

  get assignCurrentGroupId(): number | null {
    if (this.assignUserId == null) return null;
    const t = this.teachers.find((x) => x.id === this.assignUserId!);
    return t?.groupId ?? null;
  }

  ngOnInit(): void {
    this.loadTeachers();
  }

  loadTeachers(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.adminService
      .getTeachers()
      .pipe(
        catchError(() => of([] as AdminUserDTO[])),
        switchMap((teachers) => {
          const teachersWithGroups$ =
            teachers.length > 0
              ? forkJoin(
                  teachers.map((t) =>
                    this.adminService.getGroupsByTeacher(t.id ?? 0).pipe(
                      map((groups) => ({ teacher: t, groups })),
                      catchError(() => of({ teacher: t, groups: [] as AdminGroupDTO[] })),
                    ),
                  ),
                )
              : of([] as { teacher: AdminUserDTO; groups: AdminGroupDTO[] }[]);
          return forkJoin({
            teachersWithGroups: teachersWithGroups$,
            groups: this.adminService.getGroups().pipe(catchError(() => of([]))),
          });
        }),
        map(({ teachersWithGroups, groups }) => ({
          teachers: teachersWithGroups.map(({ teacher, groups: grps }) => toTeacher(teacher, grps)),
          groups: groups.map((d) => ({
            id: d.id ?? 0,
            name: d.name ?? '',
            language: d.language?.name ?? '',
            teacherName: '—',
            studentsCount: 0,
          })),
        })),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка загрузки преподавателей';
          this.cdr.detectChanges();
          return of({ teachers: [] as Teacher[], groups: [] as Group[] });
        }),
      )
      .subscribe({
        next: ({ teachers, groups }) => {
          this.teachers = teachers;
          this.groups = groups;
          this.cdr.detectChanges();
        },
      });
  }

  openAdd(): void {
    this.editingId = null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  openEdit(t: Teacher): void {
    this.editingId = t.id;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  onSave(value: {
    firstName: string;
    lastName: string;
    email: string;
    password?: string;
    groupId?: number | null;
  }): void {
    const id = this.editingId;
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const done = () => {
      this.saving = false;
      this.showForm = false;
      this.editingId = null;
      this.loadTeachers();
    };

    if (id !== null) {
      this.adminService
        .updateTeacher(id, { firstName: value.firstName, lastName: value.lastName })
        .pipe(
          finalize(() => {
            this.saving = false;
            this.cdr.detectChanges();
          }),
          catchError((err) => {
            this.error = err?.message ?? 'Ошибка сохранения';
            this.cdr.detectChanges();
            return EMPTY;
          }),
        )
        .subscribe({ next: () => done() });
    } else {
      const groupId = value.groupId;
      this.adminService
        .createTeacher({
          firstName: value.firstName,
          lastName: value.lastName,
          email: value.email,
          password: value.password ?? '',
        })
        .pipe(
          concatMap((created) => {
            if (groupId != null && created.id != null) {
              return this.adminService.addStudentToGroup(groupId, created.id).pipe(map(() => created));
            }
            return of(created);
          }),
          finalize(() => {
            this.saving = false;
            this.cdr.detectChanges();
          }),
          catchError((err) => {
            this.error = err?.message ?? 'Ошибка создания';
            this.cdr.detectChanges();
            return EMPTY;
          }),
        )
        .subscribe({ next: () => done() });
    }
  }

  onCancel(): void {
    this.showForm = false;
    this.editingId = null;
    this.cdr.detectChanges();
  }

  deleteTeacher(id: number): void {
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();
    this.adminService
      .deleteTeacher(id)
      .pipe(
        finalize(() => {
          this.saving = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка удаления';
          this.cdr.detectChanges();
          return EMPTY;
        }),
      )
      .subscribe({ next: () => this.loadTeachers() });
  }

  openAssignModal(t: Teacher): void {
    this.assignUserId = t.id;
    this.showAssignModal = true;
    this.cdr.detectChanges();
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.assignUserId = null;
    this.cdr.detectChanges();
  }

  onAssignSave(groupId: number): void {
    const userId = this.assignUserId;
    if (userId == null) return;
    const t = this.teachers.find((x) => x.id === userId);
    const oldGroupId = t?.groupId ?? null;

    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const remove$ =
      oldGroupId != null
        ? this.adminService.removeStudentFromGroup(oldGroupId, userId)
        : of(undefined as unknown);
    remove$
      .pipe(
        concatMap(() => this.adminService.addStudentToGroup(groupId, userId)),
        finalize(() => {
          this.saving = false;
          this.closeAssignModal();
          this.loadTeachers();
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка привязки к группе';
          this.cdr.detectChanges();
          return EMPTY;
        }),
      )
      .subscribe();
  }
}
