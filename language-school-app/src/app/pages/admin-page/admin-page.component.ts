import { Component, inject, signal, ChangeDetectionStrategy, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import { AuthService } from '../../core/auth/auth.service';
import { GroupsTabComponent } from './groups-tab/groups-tab.component';
import { StudentsTabComponent } from './students-tab/students-tab.component';
import { TeachersTabComponent } from './teachers-tab/teachers-tab.component';
import { LanguagesTabComponent } from './languages-tab/languages-tab.component';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [
    HeaderComponent,
    TabsComponent,
    GroupsTabComponent,
    StudentsTabComponent,
    TeachersTabComponent,
    LanguagesTabComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.less',
})
export class AdminPageComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly tabs = [
    { id: 'groups', label: 'Курсы' },
    { id: 'students', label: 'Студенты' },
    { id: 'teachers', label: 'Преподаватели' },
    { id: 'languages', label: 'Языки' },
  ];

  activeTab = signal('groups');

  onTabChange(tabId: string): void {
    this.activeTab.set(tabId);
  }

  onLogout(): void {
    this.authService.logout().pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }
}
