import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { GroupFormComponent, type GroupFormValue } from '../group-form/group-form.component';
import type { Group } from '../admin-page.models';
import { AdminService, type AdminGroupDTO, type AdminUserDTO } from '../../../core/admin/admin.service';
import { catchError, EMPTY, finalize, forkJoin, map, of } from 'rxjs';

function toGroup(dto: AdminGroupDTO): Group {
  return {
    id: dto.id ?? '',
    name: dto.name ?? '',
    language: dto.language?.name ?? '',
    teacherName: '—',
    studentsCount: 0,
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
  languages: { id?: string; name?: string }[] = [];
  teachers: { id?: string; fullName?: string }[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  showForm = false;
  editingId: string | null = null;

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

    forkJoin({
      groups: this.adminService.getGroups().pipe(catchError(() => of([] as AdminGroupDTO[]))),
      languages: this.adminService.getLanguages().pipe(catchError(() => of([]))),
      teachers: this.adminService.getTeachers().pipe(catchError(() => of([] as AdminUserDTO[]))),
    })
      .pipe(
        map(({ groups, languages, teachers }) => ({
          groups: groups.map(toGroup),
          languages,
          teachers: teachers.map((t) => ({
            id: t.id,
            fullName: [t.firstName, t.lastName].filter(Boolean).join(' ') || '—',
          })),
        })),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка загрузки данных';
          this.cdr.detectChanges();
          return of({ groups: [] as Group[], languages: [], teachers: [] });
        }),
      )
      .subscribe({
        next: ({ groups, languages, teachers }) => {
          this.groups = groups;
          this.languages = languages;
          this.teachers = teachers;
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

  onSave(value: GroupFormValue): void {
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
        ? this.adminService.editGroup(id, value.name)
        : this.adminService.createGroup(value.name, value.teacherId, value.languageId);

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

  deleteGroup(id: string): void {
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
