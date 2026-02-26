import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { InputComponent } from '../../shared/ui/input/input.component';
import { LabelComponent } from '../../shared/ui/label/label.component';
import { CardComponent } from '../../shared/ui/card/card.component';
import { CardHeaderComponent } from '../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../shared/ui/card/card-title/card-title.component';
import { CardContentComponent } from '../../shared/ui/card/card-content/card-content.component';
import type { Group, Student, Teacher, Language } from './admin-page.models';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    HeaderComponent,
    TabsComponent,
    ButtonComponent,
    InputComponent,
    LabelComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
  ],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.less',
})
export class AdminPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly tabs = [
    { id: 'groups', label: 'Группы' },
    { id: 'students', label: 'Студенты' },
    { id: 'teachers', label: 'Преподаватели' },
    { id: 'languages', label: 'Языки' },
  ];

  activeTab = signal('groups');

  readonly groups = signal<Group[]>([
    { id: 1, name: 'Английский B1', language: 'Английский', teacherName: 'Смирнова Е.В.', studentsCount: 12 },
    { id: 2, name: 'Немецкий A2', language: 'Немецкий', teacherName: 'Петров А.С.', studentsCount: 8 },
  ]);
  readonly students = signal<Student[]>([
    { id: 1, fullName: 'Иванов Иван Иванович', email: 'ivanov@example.com', groupName: 'Английский B1' },
    { id: 2, fullName: 'Петрова Мария Сергеевна', email: 'petrova@example.com', groupName: 'Немецкий A2' },
  ]);
  readonly teachers = signal<Teacher[]>([
    { id: 1, fullName: 'Смирнова Елена Владимировна', email: 'smirnova@school.com', languages: 'Английский' },
    { id: 2, fullName: 'Петров Александр Сергеевич', email: 'petrov@school.com', languages: 'Немецкий' },
  ]);
  readonly languages = signal<Language[]>([
    { id: 1, name: 'Английский' },
    { id: 2, name: 'Немецкий' },
    { id: 3, name: 'Французский' },
  ]);

  showGroupForm = signal(false);
  editingGroupId = signal<number | null>(null);
  showStudentForm = signal(false);
  editingStudentId = signal<number | null>(null);
  showTeacherForm = signal(false);
  editingTeacherId = signal<number | null>(null);
  showLanguageForm = signal(false);
  editingLanguageId = signal<number | null>(null);

  formGroup = this.fb.nonNullable.group({
    name: ['', Validators.required],
    language: ['', Validators.required],
    teacherName: ['', Validators.required],
  });
  formStudent = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    groupName: ['', Validators.required],
  });
  formTeacher = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    languages: ['', Validators.required],
  });
  formLanguage = this.fb.nonNullable.group({
    name: ['', Validators.required],
  });

  onTabChange(tabId: string): void {
    this.activeTab.set(tabId);
  }

  onLogout(): void {
    this.router.navigateByUrl('/');
  }

  openAddGroup(): void {
    this.editingGroupId.set(null);
    this.formGroup.reset({ name: '', language: '', teacherName: '' });
    this.showGroupForm.set(true);
  }
  openEditGroup(g: Group): void {
    this.editingGroupId.set(g.id);
    this.formGroup.setValue({ name: g.name, language: g.language, teacherName: g.teacherName });
    this.showGroupForm.set(true);
  }
  saveGroup(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }
    const v = this.formGroup.getRawValue();
    const id = this.editingGroupId();
    if (id !== null) {
      this.groups.update(list =>
        list.map(g => (g.id === id ? { ...g, ...v, studentsCount: g.studentsCount } : g))
      );
    } else {
      this.groups.update(list => [...list, { id: Date.now(), ...v, studentsCount: 0 }]);
    }
    this.showGroupForm.set(false);
    this.editingGroupId.set(null);
  }
  cancelGroupForm(): void {
    this.showGroupForm.set(false);
    this.editingGroupId.set(null);
  }
  deleteGroup(id: number): void {
    this.groups.update(list => list.filter(g => g.id !== id));
  }

  openAddStudent(): void {
    this.editingStudentId.set(null);
    this.formStudent.reset({ fullName: '', email: '', groupName: '' });
    this.showStudentForm.set(true);
  }
  openEditStudent(s: Student): void {
    this.editingStudentId.set(s.id);
    this.formStudent.setValue({ fullName: s.fullName, email: s.email, groupName: s.groupName });
    this.showStudentForm.set(true);
  }
  saveStudent(): void {
    if (this.formStudent.invalid) {
      this.formStudent.markAllAsTouched();
      return;
    }
    const v = this.formStudent.getRawValue();
    const id = this.editingStudentId();
    if (id !== null) {
      this.students.update(list => list.map(s => (s.id === id ? { ...s, ...v } : s)));
    } else {
      this.students.update(list => [...list, { id: Date.now(), ...v }]);
    }
    this.showStudentForm.set(false);
    this.editingStudentId.set(null);
  }
  cancelStudentForm(): void {
    this.showStudentForm.set(false);
    this.editingStudentId.set(null);
  }
  deleteStudent(id: number): void {
    this.students.update(list => list.filter(s => s.id !== id));
  }

  openAddTeacher(): void {
    this.editingTeacherId.set(null);
    this.formTeacher.reset({ fullName: '', email: '', languages: '' });
    this.showTeacherForm.set(true);
  }
  openEditTeacher(t: Teacher): void {
    this.editingTeacherId.set(t.id);
    this.formTeacher.setValue({ fullName: t.fullName, email: t.email, languages: t.languages });
    this.showTeacherForm.set(true);
  }
  saveTeacher(): void {
    if (this.formTeacher.invalid) {
      this.formTeacher.markAllAsTouched();
      return;
    }
    const v = this.formTeacher.getRawValue();
    const id = this.editingTeacherId();
    if (id !== null) {
      this.teachers.update(list => list.map(t => (t.id === id ? { ...t, ...v } : t)));
    } else {
      this.teachers.update(list => [...list, { id: Date.now(), ...v }]);
    }
    this.showTeacherForm.set(false);
    this.editingTeacherId.set(null);
  }
  cancelTeacherForm(): void {
    this.showTeacherForm.set(false);
    this.editingTeacherId.set(null);
  }
  deleteTeacher(id: number): void {
    this.teachers.update(list => list.filter(t => t.id !== id));
  }

  openAddLanguage(): void {
    this.editingLanguageId.set(null);
    this.formLanguage.reset({ name: '' });
    this.showLanguageForm.set(true);
  }
  openEditLanguage(l: Language): void {
    this.editingLanguageId.set(l.id);
    this.formLanguage.setValue({ name: l.name });
    this.showLanguageForm.set(true);
  }
  saveLanguage(): void {
    if (this.formLanguage.invalid) {
      this.formLanguage.markAllAsTouched();
      return;
    }
    const v = this.formLanguage.getRawValue();
    const id = this.editingLanguageId();
    if (id !== null) {
      this.languages.update(list => list.map(l => (l.id === id ? { ...l, ...v } : l)));
    } else {
      this.languages.update(list => [...list, { id: Date.now(), ...v }]);
    }
    this.showLanguageForm.set(false);
    this.editingLanguageId.set(null);
  }
  cancelLanguageForm(): void {
    this.showLanguageForm.set(false);
    this.editingLanguageId.set(null);
  }
  deleteLanguage(id: number): void {
    this.languages.update(list => list.filter(l => l.id !== id));
  }
}
