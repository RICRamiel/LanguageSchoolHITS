import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { GroupFormComponent } from '../group-form/group-form.component';
import type { Group } from '../admin-page.models';
import { AdminService, type AdminGroupDTO, type AdminLanguageDTO, type AdminUserDTO } from '../../../core/admin/admin.service';
import { catchError, EMPTY, finalize, forkJoin, map, of, switchMap } from 'rxjs';

function toGroup(
  dto: AdminGroupDTO,
  studentsCount: number,
  teacherName: string,
): Group {
  return {
    id: dto.id ?? 0,
    name: dto.name ?? '',
    language: dto.language?.name ?? '',
    teacherName: teacherName || '—',
    studentsCount,
  };
}

@Component({
  selector: 'app-groups-tab',
  standalone: true,
  imports: [ButtonComponent, GroupFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './groups-tab.component.html',
  styleUrl: './groups-tab.component.less',
})
export class GroupsTabComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  groups: Group[] = [];
  languages: AdminLanguageDTO[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  showForm = false;
  editingId: number | null = null;

  get editingGroup(): Group | null {
    if (this.editingId === null) return null;
    return this.groups.find((g) => g.id === this.editingId!) ?? null;
  }

  ngOnInit(): void {
    this.loadGroups();
  }

  loadGroups(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.adminService
      .getGroups()
      .pipe(
        switchMap((groupDtos) => {
          const studentCounts$ = groupDtos.map((g) =>
            this.adminService.getStudentsByGroupId(g.id ?? 0).pipe(
              map((students) => students.length),
              catchError(() => of(0)),
            ),
          );
          return this.adminService.getTeachers().pipe(
            catchError(() => of([] as AdminUserDTO[])),
            switchMap((teachers) => {
              const groupsByTeacher$ =
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
                groups: of(groupDtos),
                languages: this.adminService.getLanguages(),
                studentCounts: forkJoin(studentCounts$),
                teachersWithGroups: groupsByTeacher$,
              });
            }),
          );
        }),
        map(({ groups, languages, studentCounts, teachersWithGroups }) => {
          const groupToTeachers = new Map<number, AdminUserDTO[]>();
          for (const { teacher, groups: grps } of teachersWithGroups) {
            for (const g of grps) {
              const gid = g.id ?? 0;
              if (!groupToTeachers.has(gid)) groupToTeachers.set(gid, []);
              groupToTeachers.get(gid)!.push(teacher);
            }
          }
          return {
            groups: groups.map((dto, i) => {
              const gid = dto.id ?? 0;
              const teachersInGroup = groupToTeachers.get(gid) ?? [];
              const teacherNames = teachersInGroup
                .map((t) => [t.lastName, t.firstName].filter(Boolean).join(' ').trim())
                .filter(Boolean)
                .join(', ');
              return toGroup(dto, studentCounts[i] ?? 0, teacherNames);
            }),
            languages,
          };
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка загрузки данных';
          this.cdr.detectChanges();
          return of({ groups: [] as Group[], languages: [] as AdminLanguageDTO[] });
        }),
      )
      .subscribe({
        next: ({ groups, languages }) => {
          this.groups = groups;
          this.languages = languages;
          this.cdr.detectChanges();
        },
      });
  }

  openAdd(): void {
    this.editingId = null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  openEdit(g: Group): void {
    this.editingId = g.id;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  onSave(value: { name: string; language: string }): void {
    const id = this.editingId;
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const done = () => {
      this.saving = false;
      this.showForm = false;
      this.editingId = null;
      this.loadGroups();
    };

    const req =
      id !== null
        ? this.adminService.editGroup(id, value.name, value.language)
        : this.adminService.createGroup(value.name, value.language);

    req
      .pipe(
        finalize(() => {
          this.saving = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? (id !== null ? 'Ошибка сохранения' : 'Ошибка создания');
          this.cdr.detectChanges();
          return EMPTY;
        }),
      )
      .subscribe({ next: () => done() });
  }

  onCancel(): void {
    this.showForm = false;
    this.editingId = null;
    this.cdr.detectChanges();
  }

  deleteGroup(id: number): void {
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();
    this.adminService
      .deleteGroup(id)
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
      .subscribe({ next: () => this.loadGroups() });
  }
}
