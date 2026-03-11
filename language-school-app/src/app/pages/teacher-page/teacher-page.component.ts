import { ChangeDetectorRef, Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, forkJoin, of, switchMap, tap } from 'rxjs';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  TaskDetailsOpenPayload,
  TeacherGroup,
  TeacherTaskComment,
  TeacherTaskDetailsSection,
  TeacherNotification,
  TeacherTask,
} from './teacher-page.types';
import { TeacherTaskListComponent } from './components/teacher-task-list/teacher-task-list.component';
import { TeacherNotificationsComponent } from './components/teacher-notifications/teacher-notifications.component';
import { CreateNotificationModalComponent } from './components/create-notification-modal/create-notification-modal.component';
import { CreateTaskModalComponent } from './components/create-task-modal/create-task-modal.component';
import { TaskDetailsModalComponent } from './components/task-details-modal/task-details-modal.component';
import { NotificationDetailsModalComponent } from './components/notification-details-modal/notification-details-modal.component';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user/user.service';
import { TeacherService } from '../../core/teacher/teacher.service';

@Component({
  selector: 'app-teacher-page',
  standalone: true,
  imports: [
    HeaderComponent,
    TabsComponent,
    TeacherTaskListComponent,
    TeacherNotificationsComponent,
    CreateNotificationModalComponent,
    CreateTaskModalComponent,
    TaskDetailsModalComponent,
    NotificationDetailsModalComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-page.component.html',
  styleUrl: './teacher-page.component.less',
})
export class TeacherPageComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly teacherService = inject(TeacherService);
  private readonly cdr = inject(ChangeDetectorRef);

  private teacherId: number | null = null;

  fullName = '';
  email = '';
  badge = 'Teacher';

  tabs = this.buildTabs(0);

  activeTab = 'tasks';
  isTaskModalOpen = false;
  isNotificationModalOpen = false;
  isTaskDetailsModalOpen = false;
  isNotificationDetailsModalOpen = false;
  selectedTask: TeacherTask | null = null;
  selectedTaskSection: TeacherTaskDetailsSection = 'overview';
  selectedNotification: TeacherNotification | null = null;

  groups: TeacherGroup[] = [];
  tasks: TeacherTask[] = [];
  notifications: TeacherNotification[] = [];

  ngOnInit(): void {
    this.loadTeacherDashboard();
  }

  onTabChange(tabId: string): void {
    this.activeTab = tabId;
  }

  onCreateTask(): void {
    this.isTaskModalOpen = true;
  }

  closeCreateTaskModal(): void {
    this.isTaskModalOpen = false;
  }

  submitTask(payload: CreateTaskPayload): void {
    this.teacherService.createTask(payload).subscribe({
      next: (task) => {
        this.tasks = [task, ...this.tasks];
        this.closeCreateTaskModal();
        this.cdr.detectChanges();
      },
    });
  }

  openCreateNotificationModal(): void {
    this.isNotificationModalOpen = true;
  }

  closeCreateNotificationModal(): void {
    this.isNotificationModalOpen = false;
  }

  submitNotification(payload: CreateNotificationPayload): void {
    this.teacherService.createNotification(payload).subscribe({
      next: (notification) => {
        this.notifications = [notification, ...this.notifications];
        this.tabs = this.buildTabs(this.notifications.length);
        this.closeCreateNotificationModal();
        this.cdr.detectChanges();
      },
    });
  }

  onOpenTaskDetails(payload: TaskDetailsOpenPayload): void {
    const task = this.tasks.find((item) => item.id === payload.taskId);
    if (!task) {
      return;
    }

    this.selectedTask = task;
    this.selectedTaskSection = payload.section;
    this.isTaskDetailsModalOpen = true;

    if (task.id > 0) {
      this.teacherService.getTaskComments(task.id).subscribe({
        next: (comments) => {
          this.applyTaskComments(task.id, comments);
        },
      });
    }
  }

  onSubmitTaskComment(text: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || taskId <= 0 || !this.teacherId) {
      return;
    }

    this.teacherService
      .createComment(taskId, this.teacherId, text)
      .pipe(switchMap(() => this.teacherService.getTaskComments(taskId)))
      .subscribe({
        next: (comments) => {
          this.applyTaskComments(taskId, comments);
        },
      });
  }

  onOpenNotification(notificationId: string): void {
    const notification = this.notifications.find((item) => item.id === notificationId);
    if (!notification) {
      return;
    }

    this.selectedNotification = notification;
    this.isNotificationDetailsModalOpen = true;
  }

  closeTaskDetailsModal(): void {
    this.selectedTask = null;
    this.selectedTaskSection = 'overview';
    this.isTaskDetailsModalOpen = false;
  }

  closeNotificationDetailsModal(): void {
    this.selectedNotification = null;
    this.isNotificationDetailsModalOpen = false;
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  private loadTeacherDashboard(): void {
    this.userService
      .getMe()
      .pipe(
        tap((profile) => {
          this.teacherId = profile.id;
          this.fullName = [profile.lastName, profile.firstName].filter(Boolean).join(' ').trim();
          this.email = profile.email;
        }),
        switchMap((profile) => {
          const fallbackGroups: TeacherGroup[] = (profile.groups ?? [])
            .map((group) => ({
              id: Number(group.id),
              name: (group.name ?? '').trim(),
            }))
            .filter((group) => Number.isFinite(group.id) && group.id > 0 && Boolean(group.name));

          const groups$ = this.teacherService
            .getGroupsByTeacher(profile.id)
            .pipe(catchError(() => of(fallbackGroups)));

          const tasks$ = this.teacherService
            .getTasksByTeacher(profile.id)
            .pipe(catchError(() => of([] as TeacherTask[])));

          return forkJoin({ groups: groups$, tasks: tasks$ }).pipe(
            switchMap(({ groups, tasks }) =>
              this.teacherService.getNotificationsByGroupIds(groups.map((group) => group.id)).pipe(
                catchError(() => of([] as TeacherNotification[])),
                switchMap((notifications) =>
                  of({
                    groups,
                    tasks,
                    notifications,
                  }),
                ),
              ),
            ),
          );
        }),
      )
      .subscribe({
        next: ({ groups, tasks, notifications }) => {
          this.groups = groups;
          this.tasks = tasks;
          this.notifications = notifications;
          this.tabs = this.buildTabs(notifications.length);
          this.cdr.detectChanges();
        },
        error: () => {
          this.groups = [];
          this.tasks = [];
          this.notifications = [];
          this.tabs = this.buildTabs(0);
          this.cdr.detectChanges();
        },
      });
  }

  private buildTabs(notificationCount: number) {
    return [
      { id: 'tasks', label: 'Задания' },
      { id: 'notifications', label: 'Уведомления', badge: notificationCount },
    ];
  }

  private applyTaskComments(taskId: number, comments: TeacherTaskComment[]): void {
    this.tasks = this.tasks.map((item) =>
      item.id === taskId
        ? {
            ...item,
            taskComments: comments,
            comments: `${comments.length} comments`,
          }
        : item,
    );

    if (this.selectedTask?.id === taskId) {
      this.selectedTask = {
        ...this.selectedTask,
        taskComments: comments,
        comments: `${comments.length} comments`,
      };
    }
  }
}


