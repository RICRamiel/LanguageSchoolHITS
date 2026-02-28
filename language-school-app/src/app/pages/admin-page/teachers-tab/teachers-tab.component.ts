import { Component, computed, signal } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { TeacherFormComponent } from '../teacher-form/teacher-form.component';
import type { Teacher } from '../admin-page.models';

@Component({
  selector: 'app-teachers-tab',
  standalone: true,
  imports: [ButtonComponent, TeacherFormComponent],
  templateUrl: './teachers-tab.component.html',
  styleUrl: './teachers-tab.component.less',
})
export class TeachersTabComponent {
  readonly teachers = signal<Teacher[]>([
    { id: 1, fullName: 'Смирнова Елена Владимировна', email: 'smirnova@school.com', languages: 'Английский' },
    { id: 2, fullName: 'Петров Александр Сергеевич', email: 'petrov@school.com', languages: 'Немецкий' },
  ]);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingTeacher = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.teachers().find(t => t.id === id) ?? null;
  });

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(t: Teacher): void {
    this.editingId.set(t.id);
    this.showForm.set(true);
  }

  onSave(value: { fullName: string; email: string; languages: string }): void {
    const id = this.editingId();
    if (id !== null) {
      this.teachers.update(list => list.map(t => (t.id === id ? { ...t, ...value } : t)));
    } else {
      this.teachers.update(list => [...list, { id: Date.now(), ...value }]);
    }
    this.showForm.set(false);
    this.editingId.set(null);
  }

  onCancel(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  deleteTeacher(id: number): void {
    this.teachers.update(list => list.filter(t => t.id !== id));
  }
}
