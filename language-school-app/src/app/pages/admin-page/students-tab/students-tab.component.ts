import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { StudentFormComponent, type StudentFormValue } from '../student-form/student-form.component';
import { AssignGroupModalComponent } from '../assign-group-modal/assign-group-modal.component';
import type { Student, Group } from '../admin-page.models';
import { AdminService, type AdminGroupDTO, type AdminUserDTO } from '../../../core/admin/admin.service';
import { catchError, concatMap, EMPTY, finalize, forkJoin, map, of, switchMap } from 'rxjs';

function toStudent(dto: AdminUserDTO, groupNameFromMap?: string): Student {
  const first = dto.firstName ?? '';
  const last = dto.lastName ?? '';
  const fullName = [first, last].filter(Boolean).join(' ') || '—';
  const groupName =
    groupNameFromMap ??
    (Array.isArray(dto.groups) ? dto.groups[0]?.name : undefined) ??
    '—';
  return {
    id: dto.id ?? 0,
    fullName,
    email: dto.email ?? '',
    groupName,
  };
}

function toGroup(dto: AdminGroupDTO): Group {
  return {
    id: dto.id ?? 0,
    name: dto.name ?? '',
    language: dto.language?.name ?? '',
    teacherName: '—',
    studentsCount: '—',
  };
}

@Component({
  selector: 'app-students-tab',
  standalone: true,
  imports: [ButtonComponent, StudentFormComponent, AssignGroupModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './students-tab.component.html',
  styleUrl: './students-tab.component.less',
})
export class StudentsTabComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  students: Student[] = [];
  groups: Group[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  showForm = false;
  editingId: number | null = null;
  showAssignModal = false;
  assignUserId: number | null = null;

  get editingStudent(): Student | null {
    if (this.editingId === null) return null;
    return this.students.find((s) => s.id === this.editingId!) ?? null;
  }

  get assignCurrentGroupId(): number | null {
    if (this.assignUserId == null) return null;
    const s = this.students.find((x) => x.id === this.assignUserId!);
    return s != null ? this.groups.find((g) => g.name === s.groupName)?.id ?? null : null;
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.adminService
      .getGroups()
      .pipe(
        switchMap((groupDtos) => {
          const studentsByGroup$ =
            groupDtos.length > 0
              ? forkJoin(
                  groupDtos.map((g) =>
                    this.adminService.getStudentsByGroupId(g.id ?? 0).pipe(
                      map((students) => {
                        const fromResponse = students[0]?.groups?.[0]?.name;
                        const groupName = (fromResponse ?? g.name ?? '—').trim() || '—';
                        return { groupName, students };
                      }),
                      catchError(() => of({ groupName: g.name ?? '—', students: [] as AdminUserDTO[] })),
                    ),
                  ),
                )
              : of([] as { groupName: string; students: AdminUserDTO[] }[]);
          return forkJoin({
            groupDtos: of(groupDtos),
            studentsByGroup: studentsByGroup$,
            students: this.adminService.getStudents(),
          });
        }),
        map(({ groupDtos, studentsByGroup, students }) => {
          const studentToGroupName = new Map<number, string>();
          for (const { groupName, students: groupStudents } of studentsByGroup) {
            for (const s of groupStudents) {
              const id = s.id ?? 0;
              if (!studentToGroupName.has(id)) studentToGroupName.set(id, groupName);
            }
          }
          return {
            students: students.map((s) => toStudent(s, studentToGroupName.get(s.id ?? 0))),
            groups: groupDtos.map(toGroup),
          };
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка загрузки данных';
          this.cdr.detectChanges();
          return of({ students: [] as Student[], groups: [] as Group[] });
        }),
      )
      .subscribe({
        next: ({ students, groups }) => {
          this.students = students;
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

  openEdit(s: Student): void {
    this.editingId = s.id;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  onSave(value: StudentFormValue): void {
    const id = this.editingId;
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const done = () => {
      this.saving = false;
      this.showForm = false;
      this.editingId = null;
      this.loadData();
    };

    if (id !== null) {
      this.adminService
        .updateStudent(id, {
          firstName: value.firstName,
          lastName: value.lastName,
        })
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
      const groupIds = value.groupId != null ? [value.groupId] : [];
      this.adminService
        .createStudent({
          firstName: value.firstName,
          lastName: value.lastName,
          email: value.email,
          password: value.password ?? '',
          groupIds,
        })
        .pipe(
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

  deleteStudent(id: number): void {
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();
    this.adminService
      .deleteStudent(id)
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
      .subscribe({ next: () => this.loadData() });
  }

  openAssignModal(s: Student): void {
    this.assignUserId = s.id;
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
    const s = this.students.find((x) => x.id === userId);
    const oldGroupId = s != null ? this.groups.find((g) => g.name === s.groupName)?.id ?? null : null;

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
          this.loadData();
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
