import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user/user.service';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { NotificationListComponent } from '../../shared/ui/notification-list/notification-list.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import { TaskCardComponent } from '../../shared/ui/task-card/task-card.component';
import { StudentTaskDetailsModalComponent } from './components/student-task-details-modal/student-task-details-modal.component';
import { StudentNotification, StudentTask } from './student-page.types';

@Component({
  selector: 'app-student-page',
  standalone: true,
  imports: [
    HeaderComponent,
    TabsComponent,
    TaskCardComponent,
    NotificationListComponent,
    StudentTaskDetailsModalComponent,
  ],
  templateUrl: './student-page.component.html',
  styleUrl: './student-page.component.less',
})
export class StudentPageComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);

  fullName = '';
  email = '';
  groupName = '';

  tabs = [
    { id: 'tasks', label: 'Задания' },
    { id: 'notifications', label: 'Уведомления', badge: 0 },
  ];

  activeTab = 'tasks';
  isTaskDetailsModalOpen = false;
  selectedTask: StudentTask | null = null;

  tasks: StudentTask[] = [];
  notifications: StudentNotification[] = [];

  ngOnInit(): void {
    this.loadCurrentUser();
  }

  onTabChange(tabId: string) {
    this.activeTab = tabId;
  }

  markNotificationAsRead(notificationId: number) {
    this.notifications = this.notifications.map((item) =>
      item.id === notificationId ? { ...item, isUnread: false } : item,
    );
  }

  openTaskDetails(taskId: number) {
    const task = this.tasks.find((item) => item.id === taskId);
    if (!task) {
      return;
    }

    this.selectedTask = task;
    this.isTaskDetailsModalOpen = true;
  }

  closeTaskDetailsModal() {
    this.selectedTask = null;
    this.isTaskDetailsModalOpen = false;
  }

  onLogout() {
    this.authService.logout().subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  private loadCurrentUser(): void {
    this.userService.getMe().subscribe({
      next: (profile) => {
        this.fullName = [profile.lastName, profile.firstName].filter(Boolean).join(' ').trim();
        this.email = profile.email;
        this.groupName = profile.groups[0]?.name ?? '';
      },
      error: () => {
        this.fullName = '';
        this.email = '';
        this.groupName = '';
      },
    });
  }
}
