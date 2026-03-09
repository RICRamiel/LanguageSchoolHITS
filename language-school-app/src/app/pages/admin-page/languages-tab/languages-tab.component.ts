import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { LanguageFormComponent } from '../language-form/language-form.component';
import type { Language } from '../admin-page.models';
import { LanguageControllerService } from '../../../api/api/languageController.service';
import { catchError, EMPTY, finalize } from 'rxjs';

type LanguageApi = { id?: number; name?: string };

function toLanguage(item: LanguageApi): Language {
  return { id: item.id ?? 0, name: item.name ?? '' };
}

@Component({
  selector: 'app-languages-tab',
  standalone: true,
  imports: [ButtonComponent, LanguageFormComponent],
  templateUrl: './languages-tab.component.html',
  styleUrl: './languages-tab.component.less',
})
export class LanguagesTabComponent implements OnInit {
  private readonly languageApi = inject(LanguageControllerService);

  readonly languages = signal<Language[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  editingLanguage = computed(() => {
    const id = this.editingId();
    if (id === null) return null;
    return this.languages().find(l => l.id === id) ?? null;
  });

  ngOnInit(): void {
    this.loadLanguages();
  }

  loadLanguages(): void {
    this.loading.set(true);
    this.error.set(null);
    this.languageApi.getLanguages()
      .pipe(
        finalize(() => this.loading.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка загрузки языков');
          return EMPTY;
        })
      )
      .subscribe({
        next: items => this.languages.set((items as LanguageApi[]).map(toLanguage)),
      });
  }

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
    this.saving.set(true);
    this.error.set(null);

    const done = () => {
      this.saving.set(false);
      this.showForm.set(false);
      this.editingId.set(null);
      this.loadLanguages();
    };

    const req = id !== null
      ? this.languageApi.editLanguageName(id, { name: value.name })
      : this.languageApi.createLanguage({ name: value.name });

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

  deleteLanguage(id: number): void {
    this.saving.set(true);
    this.error.set(null);
    this.languageApi.deleteLanguage(id)
      .pipe(
        finalize(() => this.saving.set(false)),
        catchError(err => {
          this.error.set(err?.message ?? 'Ошибка удаления');
          return EMPTY;
        })
      )
      .subscribe({
        next: () => this.loadLanguages(),
      });
  }
}
