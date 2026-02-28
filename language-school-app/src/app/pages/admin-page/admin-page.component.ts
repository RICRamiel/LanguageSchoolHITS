import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
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
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.less',
})
export class AdminPageComponent {
  private readonly router = inject(Router);

  readonly tabs = [
    { id: 'groups', label: 'Группы' },
    { id: 'students', label: 'Студенты' },
    { id: 'teachers', label: 'Преподаватели' },
    { id: 'languages', label: 'Языки' },
  ];

  activeTab = signal('groups');

  onTabChange(tabId: string): void {
    this.activeTab.set(tabId);
  }

  onLogout(): void {
    this.router.navigateByUrl('/');
  }
}
