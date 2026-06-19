import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { StudentFormComponent, type StudentFormValue } from '../student-form/student-form.component';
import { AssignGroupModalComponent } from '../assign-group-modal/assign-group-modal.component';
import type { Student, Group } from '../admin-page.models';
import { AdminService, type AdminGroupDTO, type AdminUserDTO } from '../../../core/admin/admin.service';
import { catchError, EMPTY, finalize, forkJoin, map, of, switchMap } from 'rxjs';

function toStudent(
  dto: AdminUserDTO,
  groupNamesFromMap?: string[],
  groupIdsFromMap?: string[],
): Student {
  const first = dto.firstName ?? '';
  const last = dto.lastName ?? '';
  const fullName = [first, last].filter(Boolean).join(' ') || '—';
  const groupNames = groupNamesFromMap?.length ? groupNamesFromMap : getGroupNames(dto);
  const groupIds = groupIdsFromMap?.length ? groupIdsFromMap : getGroupIds(dto);

  return {
    id: dto.id ?? '',
    fullName,
    email: dto.email ?? '',
    groupName: groupNames.length > 0 ? groupNames.join(', ') : '—',
    groupIds,
  };
}

function getGroupNames(dto: AdminUserDTO): string[] {
  if (!Array.isArray(dto.groups)) {
    return [];
  }

  return unique(dto.groups.map((group) => group.name?.trim()).filter((name): name is string => !!name));
}

function getGroupIds(dto: AdminUserDTO): string[] {
  if (!Array.isArray(dto.groups)) {
    return [];
  }

  return unique(dto.groups.map((group) => group.id).filter((id): id is string => !!id));
}

function unique(values: string[]): string[] {
  return [...new Set(values)];
}

function toGroup(dto: AdminGroupDTO): Group {
  return {
    id: dto.id ?? '',
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
  editingId: string | null = null;
  showAssignModal = false;
  assignUserId: string | null = null;

  get editingStudent(): Student | null {
    if (this.editingId === null) return null;
    return this.students.find((s) => s.id === this.editingId!) ?? null;
  }

  get assignCurrentGroupIds(): string[] {
    if (this.assignUserId == null) return [];
    const s = this.students.find((x) => x.id === this.assignUserId!);
    return s?.groupIds ?? [];
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
                    this.adminService.getStudentsByGroupId(g.id ?? '').pipe(
                      map((students) => {
                        const groupName = (g.name ?? '—').trim() || '—';
                        return { groupId: g.id ?? '', groupName, students };
                      }),
                      catchError(() =>
                        of({ groupId: g.id ?? '', groupName: g.name ?? '—', students: [] as AdminUserDTO[] }),
                      ),
                    ),
                  ),
                )
              : of([] as { groupId: string; groupName: string; students: AdminUserDTO[] }[]);
          return forkJoin({
            groupDtos: of(groupDtos),
            studentsByGroup: studentsByGroup$,
            students: this.adminService.getStudents(),
          });
        }),
        map(({ groupDtos, studentsByGroup, students }) => {
          const studentToGroupNames = new Map<string, string[]>();
          const studentToGroupIds = new Map<string, string[]>();
          for (const { groupId, groupName, students: groupStudents } of studentsByGroup) {
            for (const s of groupStudents) {
              const id = s.id ?? '';
              if (!studentToGroupNames.has(id)) {
                studentToGroupNames.set(id, []);
                studentToGroupIds.set(id, []);
              }
              if (groupName && !studentToGroupNames.get(id)!.includes(groupName)) {
                studentToGroupNames.get(id)!.push(groupName);
              }
              if (groupId && !studentToGroupIds.get(id)!.includes(groupId)) {
                studentToGroupIds.get(id)!.push(groupId);
              }
            }
          }
          return {
            students: students.map((s) => {
              const id = s.id ?? '';
              const names = studentToGroupNames.get(id);
              const ids = studentToGroupIds.get(id);
              return toStudent(s, names, ids);
            }),
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

  deleteStudent(id: string): void {
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

  onAssignSave(groupId: string): void {
    const userId = this.assignUserId;
    if (userId == null) return;

    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    this.adminService
      .addStudentToGroup(groupId, userId)
      .pipe(
        finalize(() => {
          this.saving = false;
          this.closeAssignModal();
          this.loadData();
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка привязки к курсу';
          this.cdr.detectChanges();
          return EMPTY;
        }),
      )
      .subscribe();
  }
}
