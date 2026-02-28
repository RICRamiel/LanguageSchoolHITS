import { Component, computed, signal } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { LanguageFormComponent } from '../language-form/language-form.component';
import type { Language } from '../admin-page.models';

@Component({
  selector: 'app-languages-tab',
  standalone: true,
  imports: [ButtonComponent, LanguageFormComponent],
  templateUrl: './languages-tab.component.html',
  styleUrl: './languages-tab.component.less',
})
export class LanguagesTabComponent {
  readonly languages = signal<Language[]>([
    { id: 1, name: 'Английский' },
    { id: 2, name: 'Немецкий' },
    { id: 3, name: 'Французский' },
  ]);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingLanguage = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.languages().find(l => l.id === id) ?? null;
  });

  openAdd(): void {
    this.editingId.set(null);
    this.showForm.set(true);
  }

  openEdit(l: Language): void {
    this.editingId.set(l.id);
    this.showForm.set(true);
  }

  onSave(value: { name: string }): void {
    const id = this.editingId();
    if (id !== null) {
      this.languages.update(list => list.map(l => (l.id === id ? { ...l, ...value } : l)));
    } else {
      this.languages.update(list => [...list, { id: Date.now(), ...value }]);
    }
    this.showForm.set(false);
    this.editingId.set(null);
  }

  onCancel(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  deleteLanguage(id: number): void {
    this.languages.update(list => list.filter(l => l.id !== id));
  }
}
