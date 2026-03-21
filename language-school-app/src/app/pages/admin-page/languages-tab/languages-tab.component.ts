import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import { LanguageFormComponent } from '../language-form/language-form.component';
import type { Language } from '../admin-page.models';
import { AdminService } from '../../../core/admin/admin.service';
import { catchError, EMPTY, finalize, map, of } from 'rxjs';

@Component({
  selector: 'app-languages-tab',
  standalone: true,
  imports: [ButtonComponent, LanguageFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './languages-tab.component.html',
  styleUrl: './languages-tab.component.less',
})
export class LanguagesTabComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  languages: Language[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  showForm = false;
  editingId: number | null = null;

  get editingLanguage(): Language | null {
    if (this.editingId === null) return null;
    return this.languages.find((l) => l.id === this.editingId!) ?? null;
  }

  ngOnInit(): void {
    this.loadLanguages();
  }

  loadLanguages(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.adminService
      .getLanguages()
      .pipe(
        map((items) =>
          items.map((dto, index) => ({
            id: (dto.id ?? index + 1),
            name: dto.name ?? '',
          })),
        ),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
        catchError((err) => {
          this.error = err?.message ?? 'Ошибка загрузки языков';
          this.cdr.detectChanges();
          return of([] as Language[]);
        }),
      )
      .subscribe({
        next: (items) => {
          this.languages = items;
          this.cdr.detectChanges();
        },
      });
  }

  openAdd(): void {
    this.editingId = null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  openEdit(l: Language): void {
    this.editingId = l.id;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  onSave(value: { name: string }): void {
    const id = this.editingId;
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const done = () => {
      this.saving = false;
      this.showForm = false;
      this.editingId = null;
      this.loadLanguages();
    };

    const req =
      id !== null
        ? this.adminService.editLanguage(id, value.name)
        : this.adminService.createLanguage(value.name);

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

  deleteLanguage(id: number): void {
    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();
    this.adminService
      .deleteLanguage(id)
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
      .subscribe({ next: () => this.loadLanguages() });
  }
}
