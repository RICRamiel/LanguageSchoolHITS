import { Component, computed, signal } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { GroupFormComponent } from '../group-form/group-form.component';
import type { Group } from '../admin-page.models';

@Component({
  selector: 'app-groups-tab',
  standalone: true,
  imports: [ButtonComponent, GroupFormComponent],
  templateUrl: './groups-tab.component.html',
  styleUrl: './groups-tab.component.less',
})
export class GroupsTabComponent {
  readonly groups = signal<Group[]>([
    { id: 1, name: 'Английский B1', language: 'Английский', teacherName: 'Смирнова Е.В.', studentsCount: 12 },
    { id: 2, name: 'Немецкий A2', language: 'Немецкий', teacherName: 'Петров А.С.', studentsCount: 8 },
  ]);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingGroup = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.groups().find(g => g.id === id) ?? null;
  });

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(g: Group): void {
    this.editingId.set(g.id);
    this.showForm.set(true);
  }

  onSave(value: { name: string; language: string; teacherName: string }): void {
    const id = this.editingId();
    if (id !== null) {
      this.groups.update(list =>
        list.map(g => (g.id === id ? { ...g, ...value } : g))
      );
    } else {
      this.groups.update(list => [...list, { id: Date.now(), ...value, studentsCount: 0 }]);
    }
    this.showForm.set(false);
    this.editingId.set(null);
  }

  onCancel(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  deleteGroup(id: number): void {
    this.groups.update(list => list.filter(g => g.id !== id));
  }
}
