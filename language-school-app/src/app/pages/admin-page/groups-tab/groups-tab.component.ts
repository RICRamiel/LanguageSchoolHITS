import { Component, computed, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { GroupFormComponent } from '../group-form/group-form.component';
import type { Group } from '../admin-page.models';
import { GroupControllerService } from '../../../api/api/groupController.service';
import type { GroupDTO } from '../../../api/model/groupDTO';
import { catchError, EMPTY, finalize } from 'rxjs';

type GroupApi = GroupDTO & { id?: number };

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
  selector: 'app-groups-tab',
  standalone: true,
  imports: [ButtonComponent, GroupFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './groups-tab.component.html',
  styleUrl: './groups-tab.component.less',
})
export class GroupsTabComponent implements OnInit {
  private readonly groupApi = inject(GroupControllerService);

  readonly groups = signal<Group[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingGroup = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.groups().find(g => g.id === id) ?? null;
  });

  ngOnInit(): void {
    this.loadGroups();
  }

  loadGroups(): void {
    this.loading.set(true);
    this.error.set(null);
    this.groupApi.getGroups()
      .pipe(
        finalize(() => this.loading.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка загрузки групп');
          return EMPTY;
        })
      )
      .subscribe({
        next: items => this.groups.set((items as GroupApi[]).map(toGroup)),
      });
  }

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(g: Group): void {
    this.editingId.set(g.id);
    this.showForm.set(true);
  }

  onSave(value: { name: string; language: string }): void {
    const id = this.editingId();
    this.saving.set(true);
    this.error.set(null);

    const body = { name: value.name, language: { name: value.language } };
    const done = () => {
      this.saving.set(false);
      this.showForm.set(false);
      this.editingId.set(null);
      this.loadGroups();
    };

    const req = id !== null
      ? this.groupApi.editGroup(id, body)
      : this.groupApi.createGroup(body);

    req.pipe(
      finalize(() => this.saving.set(false)),
      catchError(err => {
        this.error.set(err?.message ?? (id !== null ? 'Ошибка сохранения' : 'Ошибка создания'));
        return EMPTY;
      })
    ).subscribe({
      next: () => done(),
    });
  }

  onCancel(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  deleteGroup(id: number): void {
    this.saving.set(true);
    this.error.set(null);
    this.groupApi.deleteGroup(id)
      .pipe(
        finalize(() => this.saving.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка удаления');
          return EMPTY;
        })
      )
      .subscribe({
        next: () => this.loadGroups(),
      });
  }
}
