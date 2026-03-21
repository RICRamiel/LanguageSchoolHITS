import { AsyncPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, combineLatest, finalize, forkJoin, map, of, shareReplay, switchMap, tap } from 'rxjs';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  NotificationAttachment,
  TaskDetailsOpenPayload,
  TeacherGroup,
  TeacherNotification,
  TeacherTask,
  TeacherTaskComment,
  TeacherTaskDetailsSection,
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
import { OPENAPI_PATHS, withOpenApiBase } from '../../core/api/openapi.config';

@Component({
  selector: 'app-teacher-page',
  standalone: true,
  imports: [
    AsyncPipe,
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
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly teacherService = inject(TeacherService);
  private readonly destroyRef = inject(DestroyRef);

  private teacherId: number | null = null;

  private readonly groupsSubject = new BehaviorSubject<TeacherGroup[]>([]);
  private readonly allTasksSubject = new BehaviorSubject<TeacherTask[]>([]);
  private readonly allNotificationsSubject = new BehaviorSubject<TeacherNotification[]>([]);
  private readonly selectedGroupFilterSubject = new BehaviorSubject<string>('all');

  private tasksSnapshot: TeacherTask[] = [];
  private notificationsSnapshot: TeacherNotification[] = [];

  fullName = '';
  email = '';
  badge = 'Teacher';
  selectedGroupFilter = 'all';

  readonly groups$ = this.groupsSubject.asObservable();

  readonly notifications$ = combineLatest([
    this.allNotificationsSubject,
    this.selectedGroupFilterSubject,
  ]).pipe(
    map(([notifications, selectedGroupFilter]) => {
      if (selectedGroupFilter === 'all') {
        return notifications;
      }
      const groupId = Number(selectedGroupFilter);
      return notifications.filter((item) => item.groupId === groupId);
    }),
    shareReplay({ bufferSize: 1, refCount: true }),
  );

  readonly tasks$ = combineLatest([
    this.selectedGroupFilterSubject,
    this.groupsSubject,
    this.allTasksSubject,
  ]).pipe(
    switchMap(([selectedGroupFilter, groups, allTasks]) => {
      if (selectedGroupFilter === 'all') {
        return of(allTasks);
      }

      const groupId = Number(selectedGroupFilter);
      const selectedGroup = groups.find((group) => group.id === groupId);
      if (!selectedGroup) {
        return of(allTasks);
      }

      return this.teacherService.getTasksByGroupName(selectedGroup.name).pipe(
        catchError(() => of([] as TeacherTask[])),
      );
    }),
    shareReplay({ bufferSize: 1, refCount: true }),
  );

  readonly tabs$ = this.notifications$.pipe(
    map((notifications) => this.buildTabs(notifications.length)),
  );

  activeTab = 'tasks';
  isTaskModalOpen = false;
  isNotificationModalOpen = false;
  isTaskDetailsModalOpen = false;
  isNotificationDetailsModalOpen = false;
  notificationAttachmentUploading = false;
  selectedTask: TeacherTask | null = null;
  selectedTaskSection: TeacherTaskDetailsSection = 'overview';
  selectedNotification: TeacherNotification | null = null;

  ngOnInit(): void {
    this.tasks$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((tasks) => {
      this.tasksSnapshot = tasks;
    });

    this.notifications$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((notifications) => {
      this.notificationsSnapshot = notifications;
    });

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
        this.allTasksSubject.next([task, ...this.allTasksSubject.value]);
        this.closeCreateTaskModal();
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
        this.allNotificationsSubject.next([notification, ...this.allNotificationsSubject.value]);
        this.closeCreateNotificationModal();
      },
    });
  }

  onOpenTaskDetails(payload: TaskDetailsOpenPayload): void {
    const task = this.tasksSnapshot.find((item) => item.id === payload.taskId);
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

  onDownloadTaskAttachment(attachment: TeacherTask['attachedWorks'][number]): void {
    if (!attachment.id) {
      return;
    }

    this.http
      .get(withOpenApiBase(OPENAPI_PATHS.attachments.download(attachment.id)), {
        observe: 'response',
        responseType: 'blob',
      })
      .pipe(
        map((response) => ({
          blob: response.body,
          fileName: this.extractFileName(response.headers.get('content-disposition')),
        })),
        catchError(() => of(null)),
      )
      .subscribe({
        next: (result) => {
          if (!result?.blob) {
            return;
          }

          this.downloadBlob(result.blob, result.fileName || attachment.fileName || 'attachment');
        },
      });
  }

  onDownloadNotificationAttachment(attachment: NotificationAttachment): void {
    if (!attachment.id) {
      return;
    }

    this.http
      .get(withOpenApiBase(OPENAPI_PATHS.attachments.download(attachment.id)), {
        observe: 'response',
        responseType: 'blob',
      })
      .pipe(
        map((response) => ({
          blob: response.body,
          fileName: this.extractFileName(response.headers.get('content-disposition')),
        })),
        catchError(() => of(null)),
      )
      .subscribe({
        next: (result) => {
          if (!result?.blob) {
            return;
          }

          this.downloadBlob(result.blob, result.fileName || attachment.fileName || 'attachment');
        },
      });
  }

  onOpenNotification(notificationId: string): void {
    const notification = this.notificationsSnapshot.find((item) => item.id === notificationId);
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
    this.notificationAttachmentUploading = false;
    this.isNotificationDetailsModalOpen = false;
  }

  onAttachNotificationFile(file: File): void {
    const notification = this.selectedNotification;
    if (!notification || this.notificationAttachmentUploading) {
      return;
    }

    this.notificationAttachmentUploading = true;

    this.teacherService
      .attachAttachmentToNotification(notification.id, file)
      .pipe(
        finalize(() => {
          this.notificationAttachmentUploading = false;
        }),
      )
      .subscribe({
        next: (attachment) => {
          if (!attachment) {
            return;
          }

          this.allNotificationsSubject.next(
            this.allNotificationsSubject.value.map((item) =>
              item.id === notification.id
                ? {
                    ...item,
                    attachment,
                  }
                : item,
            ),
          );

          if (this.selectedNotification?.id === notification.id) {
            this.selectedNotification = {
              ...this.selectedNotification,
              attachment,
            };
          }
        },
      });
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  onGroupFilterChange(value: string): void {
    this.selectedGroupFilter = value;
    this.selectedGroupFilterSubject.next(value);
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
                map((notifications) => ({
                  groups,
                  tasks,
                  notifications,
                })),
              ),
            ),
          );
        }),
      )
      .subscribe({
        next: ({ groups, tasks, notifications }) => {
          this.groupsSubject.next(groups);
          this.allTasksSubject.next(tasks);
          this.allNotificationsSubject.next(notifications);
        },
        error: () => {
          this.groupsSubject.next([]);
          this.allTasksSubject.next([]);
          this.allNotificationsSubject.next([]);
          this.selectedGroupFilter = 'all';
          this.selectedGroupFilterSubject.next('all');
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
    this.allTasksSubject.next(
      this.allTasksSubject.value.map((item) =>
        item.id === taskId
          ? {
              ...item,
              taskComments: comments,
              comments: `${comments.length} comments`,
            }
          : item,
      ),
    );

    if (this.selectedTask?.id === taskId) {
      this.selectedTask = {
        ...this.selectedTask,
        taskComments: comments,
        comments: `${comments.length} comments`,
      };
    }
  }

  private extractFileName(contentDisposition: string | null): string {
    if (!contentDisposition) {
      return '';
    }

    const utf8Name = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    if (utf8Name) {
      return decodeURIComponent(utf8Name).trim();
    }

    const plainName = contentDisposition.match(/filename="?([^";]+)"?/i)?.[1];
    return plainName?.trim() ?? '';
  }

  private downloadBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  }
}



