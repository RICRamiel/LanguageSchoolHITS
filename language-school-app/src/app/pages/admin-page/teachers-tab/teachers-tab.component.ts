import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { catchError, EMPTY, finalize, forkJoin, map, of } from 'rxjs';
import { AdminService, type AdminGroupDTO, type AdminUserDTO } from '../../../core/admin/admin.service';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import type { Group, Teacher } from '../admin-page.models';
import { TeacherFormComponent } from '../teacher-form/teacher-form.component';

function toTeacher(dto: AdminUserDTO, groups: AdminGroupDTO[]): Teacher {
  const first = dto.firstName ?? '';
  const last = dto.lastName ?? '';
  const fullName = [first, last].filter(Boolean).join(' ') || 'вЂ”';
  const assignedGroups = groups.filter((group) => {
    const teacherId = group.teacher?.id ?? null;
    if (teacherId != null && dto.id != null && String(teacherId) === String(dto.id)) {
      return true;
    }

    return (dto.groups ?? []).some((userGroup) => String(userGroup.id ?? '') === String(group.id ?? ''));
  });
  const languages = [
    ...new Set(assignedGroups.map((group) => group.language?.name).filter((name): name is string => !!name)),
  ].join(', ');

  return {
    id: dto.id ?? '',
    fullName,
    email: dto.email ?? '',
    languages: languages || 'вЂ”',
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
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  teachers: Teacher[] = [];
  groups: Group[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  showForm = false;
  editingId: string | null = null;

  get editingTeacher(): Teacher | null {
    if (this.editingId === null) return null;
    return this.teachers.find((teacher) => teacher.id === this.editingId) ?? null;
  }

  ngOnInit(): void {
    this.loadTeachers();
  }

  loadTeachers(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    forkJoin({
      teachers: this.adminService.getTeachers().pipe(catchError(() => of([] as AdminUserDTO[]))),
      groups: this.adminService.getGroups().pipe(catchError(() => of([] as AdminGroupDTO[]))),
    })
      .pipe(
        map(({ teachers, groups }) => ({
          teachers: teachers.map((teacher) => toTeacher(teacher, groups)),
          groups: groups.map((group) => ({
            id: group.id ?? '',
            name: group.name ?? '',
            language: group.language?.name ?? '',
            teacherName: 'вЂ”',
            studentsCount: 0,
          })),
        })),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'РћС€РёР±РєР° Р·Р°РіСЂСѓР·РєРё РїСЂРµРїРѕРґР°РІР°С‚РµР»РµР№';
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

  openEdit(teacher: Teacher): void {
    this.editingId = teacher.id;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  onSave(value: {
    firstName: string;
    lastName: string;
    email: string;
    password?: string;
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
            this.error = err?.message ?? 'РћС€РёР±РєР° СЃРѕС…СЂР°РЅРµРЅРёСЏ';
            this.cdr.detectChanges();
            return EMPTY;
          }),
        )
        .subscribe({ next: () => done() });
      return;
    }

    this.adminService
      .createTeacher({
        firstName: value.firstName,
        lastName: value.lastName,
        email: value.email,
        password: value.password ?? '',
      })
      .pipe(
        finalize(() => {
          this.saving = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'РћС€РёР±РєР° СЃРѕР·РґР°РЅРёСЏ';
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

  deleteTeacher(id: string): void {
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
          this.error = err?.message ?? 'РћС€РёР±РєР° СѓРґР°Р»РµРЅРёСЏ';
          this.cdr.detectChanges();
          return EMPTY;
        }),
      )
      .subscribe({ next: () => this.loadTeachers() });
  }
}
