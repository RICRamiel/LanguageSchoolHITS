import { Component, computed, signal } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { StudentFormComponent } from '../student-form/student-form.component';
import type { Student } from '../admin-page.models';

@Component({
  selector: 'app-students-tab',
  standalone: true,
  imports: [ButtonComponent, StudentFormComponent],
  templateUrl: './students-tab.component.html',
  styleUrl: './students-tab.component.less',
})
export class StudentsTabComponent {
  readonly students = signal<Student[]>([
    { id: 1, fullName: 'Иванов Иван Иванович', email: 'ivanov@example.com', groupName: 'Английский B1' },
    { id: 2, fullName: 'Петрова Мария Сергеевна', email: 'petrova@example.com', groupName: 'Немецкий A2' },
  ]);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingStudent = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.students().find(s => s.id === id) ?? null;
  });

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(s: Student): void {
    this.editingId.set(s.id);
    this.showForm.set(true);
  }

  onSave(value: { fullName: string; email: string; groupName: string }): void {
    const id = this.editingId();
    if (id !== null) {
      this.students.update(list => list.map(s => (s.id === id ? { ...s, ...value } : s)));
    } else {
      this.students.update(list => [...list, { id: Date.now(), ...value }]);
    }
    this.showForm.set(false);
    this.editingId.set(null);
  }

  onCancel(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  deleteStudent(id: number): void {
    this.students.update(list => list.filter(s => s.id !== id));
  }
}
