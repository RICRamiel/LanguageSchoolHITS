import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, finalize, forkJoin, map, of, switchMap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user/user.service';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { NotificationListComponent } from '../../shared/ui/notification-list/notification-list.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import { TaskCardComponent } from '../../shared/ui/task-card/task-card.component';
import { StudentTaskDetailsModalComponent } from './components/student-task-details-modal/student-task-details-modal.component';
import { StudentNotification, StudentTask, StudentTaskComment } from './student-page.types';
import { OPENAPI_PATHS, withOpenApiBase } from '../../core/api/openapi.config';
import { AttachmentControllerService, TaskControllerService } from '../../api';
import { CommentDTO } from '../../api/model/commentDTO';

type StudentNotificationResponse = {
  id?: string;
  text?: string;
  creationDate?: string;
};

type StudentTaskResponse = {
  id?: number;
  name?: string;
  description?: string;
  deadline?: string;
  taskStatus?: 'COMPLETE' | 'OVERDUE' | 'PENDING';
  teacher?: {
    firstName?: string;
    lastName?: string;
  };
};

const RU = {
  tasks: '\u0417\u0430\u0434\u0430\u043D\u0438\u044F',
  notifications: '\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u044F',
  task: '\u0417\u0430\u0434\u0430\u043D\u0438\u0435',
  notification: '\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u0435',
  teacher: '\u041F\u0440\u0435\u043F\u043E\u0434\u0430\u0432\u0430\u0442\u0435\u043B\u044C',
  group: '\u0413\u0440\u0443\u043F\u043F\u0430',
  completed: '\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u043E',
  overdue: '\u041F\u0440\u043E\u0441\u0440\u043E\u0447\u0435\u043D\u043E',
  inProgress: '\u0412 \u043F\u0440\u043E\u0446\u0435\u0441\u0441\u0435',
} as const;

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
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './student-page.component.html',
  styleUrl: './student-page.component.less',
})
export class StudentPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly attachmentApi = inject(AttachmentControllerService);
  private readonly taskApi = inject(TaskControllerService);
  private readonly cdr = inject(ChangeDetectorRef);

  studentId = 0;
  fullName = '';
  email = '';
  groupName = '';

  tabs = [
    { id: 'tasks', label: RU.tasks },
    { id: 'notifications', label: RU.notifications, badge: 0 },
  ];

  activeTab = 'tasks';
  isTaskDetailsModalOpen = false;
  selectedTask: StudentTask | null = null;

  tasks: StudentTask[] = [];
  notifications: StudentNotification[] = [];

  uploadedFileLink: string | null = null;
  uploadInProgress = false;
  completeInProgress = false;
  commentsLoading = false;
  commentSubmitting = false;
  taskComments: StudentTaskComment[] = [];

  ngOnInit(): void {
    this.loadCurrentUser();
  }

  onTabChange(tabId: string) {
    this.activeTab = tabId;
  }

  openTaskDetails(taskId: number) {
    const task = this.tasks.find((item) => item.id === taskId);
    if (!task) {
      return;
    }

    this.selectedTask = task;
    this.isTaskDetailsModalOpen = true;
    this.uploadedFileLink = null;
    this.loadTaskComments(taskId);
  }

  closeTaskDetailsModal() {
    this.selectedTask = null;
    this.isTaskDetailsModalOpen = false;
    this.uploadedFileLink = null;
    this.uploadInProgress = false;
    this.completeInProgress = false;
    this.commentsLoading = false;
    this.commentSubmitting = false;
    this.taskComments = [];
  }

  onUploadTaskFile(file: File): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.uploadInProgress) {
      return;
    }

    this.uploadInProgress = true;
    this.cdr.detectChanges();

    this.attachmentApi
      .uploadAttachment(taskId, file)
      .pipe(
        switchMap((response) => {
          const attachmentId = this.extractAttachmentId(response);
          if (!attachmentId) {
            return of(null as string | null);
          }
          return this.attachmentApi.getDownloadLink(attachmentId).pipe(
            catchError(() => of(null as string | null)),
          );
        }),
        finalize(() => {
          this.uploadInProgress = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (downloadLink) => {
          if (downloadLink) {
            this.uploadedFileLink = downloadLink;
          }
        },
      });
  }

  onCompleteTask(): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.completeInProgress) {
      return;
    }

    this.completeInProgress = true;
    this.cdr.detectChanges();

    this.taskApi
      .completeTask(taskId)
      .pipe(
        finalize(() => {
          this.completeInProgress = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: () => {
          this.tasks = this.tasks.map((task) =>
            task.id === taskId
              ? { ...task, pillText: RU.completed, pillVariant: 'success' }
              : task,
          );

          if (this.selectedTask?.id === taskId) {
            this.selectedTask = {
              ...this.selectedTask,
              pillText: RU.completed,
              pillVariant: 'success',
            };
          }
          this.cdr.detectChanges();
        },
      });
  }

  onSubmitComment(text: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || !this.studentId || this.commentSubmitting) {
      return;
    }

    this.commentSubmitting = true;
    this.cdr.detectChanges();

    this.http
      .post<CommentDTO>(withOpenApiBase(OPENAPI_PATHS.comments.create), {
        text,
        taskId,
        userId: this.studentId,
        privateStatus: false,
      })
      .pipe(
        switchMap((createdComment) =>
          this.http
            .get<unknown>(withOpenApiBase(OPENAPI_PATHS.comments.byTask(taskId)))
            .pipe(
            map((comments) => this.mapTaskComments(comments)),
            catchError(() =>
              of([...this.taskComments, this.mapTaskComment(createdComment)]),
            ),
            ),
        ),
        finalize(() => {
          this.commentSubmitting = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (comments) => {
          this.taskComments = comments;
          this.cdr.detectChanges();
        },
      });
  }

  onLogout() {
    this.authService.logout().subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  private loadCurrentUser(): void {
    this.userService
      .getMe()
      .pipe(
        switchMap((profile) => {
          this.fullName = [profile.lastName, profile.firstName].filter(Boolean).join(' ').trim();
          this.email = profile.email;
          this.studentId = profile.id;
          this.groupName = profile.groups[0]?.name ?? '';

          const tasks$ = this.groupName
            ? this.http
                .get<StudentTaskResponse[]>(
                  withOpenApiBase(OPENAPI_PATHS.tasks.byGroupName(this.groupName)),
                )
                .pipe(catchError(() => of([] as StudentTaskResponse[])))
            : of([] as StudentTaskResponse[]);

          const notifications$ = this.http
            .get<StudentNotificationResponse[]>(
              withOpenApiBase(OPENAPI_PATHS.notifications.forStudent(profile.id)),
            )
            .pipe(catchError(() => of([] as StudentNotificationResponse[])));

          return forkJoin({
            tasks: tasks$,
            notifications: notifications$,
          });
        }),
      )
      .subscribe({
        next: ({ tasks, notifications }) => {
          this.tasks = tasks.map((task) => this.mapTask(task));
          this.notifications = notifications.map((item, index) =>
            this.mapNotification(item, index),
          );
          this.tabs = [
            { id: 'tasks', label: RU.tasks },
            { id: 'notifications', label: RU.notifications, badge: this.notifications.length },
          ];
          this.cdr.detectChanges();
        },
        error: () => {
          this.fullName = '';
          this.email = '';
          this.studentId = 0;
          this.groupName = '';
          this.notifications = [];
          this.tabs = [
            { id: 'tasks', label: RU.tasks },
            { id: 'notifications', label: RU.notifications, badge: 0 },
          ];
          this.cdr.detectChanges();
        },
      });
  }

  private mapTask(task: StudentTaskResponse | null | undefined): StudentTask {
    const title = (task?.name ?? '').trim() || RU.task;
    const description = (task?.description ?? '').trim();
    const status = task?.taskStatus ?? 'PENDING';
    const teacherName = [task?.teacher?.lastName, task?.teacher?.firstName]
      .filter(Boolean)
      .join(' ')
      .trim();

    return {
      id: Number(task?.id) || Date.now(),
      title,
      pillText: this.mapTaskStatusLabel(status),
      pillVariant: status === 'COMPLETE' ? 'success' : 'neutral',
      teacher: teacherName || RU.teacher,
      description,
      dueText: this.formatDate(task?.deadline),
    };
  }

  private loadTaskComments(taskId: number): void {
    this.commentsLoading = true;
    this.taskComments = [];
    this.cdr.detectChanges();

    this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.comments.byTask(taskId)))
      .pipe(
        catchError(() => of([])),
        finalize(() => {
          this.commentsLoading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (comments) => {
          this.taskComments = this.mapTaskComments(comments);
        },
      });
  }

  private mapTaskComments(comments: unknown): StudentTaskComment[] {
    return this.normalizeComments(comments).map((comment) => ({
      userId: comment.userId ?? null,
      text: this.normalizeText(comment.text),
    }));
  }

  private mapTaskComment(comment: CommentDTO | null | undefined): StudentTaskComment {
    return {
      userId: comment?.userId ?? null,
      text: this.normalizeText(comment?.text),
    };
  }

  private normalizeComments(payload: unknown): CommentDTO[] {
    if (typeof payload === 'string') {
      const parsed = this.safeJsonParse(payload);
      if (parsed !== null) {
        return this.normalizeComments(parsed);
      }
      return [];
    }

    if (Array.isArray(payload)) {
      return payload
        .filter((item) => this.isCommentLike(item))
        .map((item) => item as CommentDTO);
    }

    if (!payload || typeof payload !== 'object') {
      return [];
    }

    const candidate = payload as Record<string, unknown>;

    if (Array.isArray(candidate['comments'])) {
      return (candidate['comments'] as unknown[])
        .filter((item) => this.isCommentLike(item))
        .map((item) => item as CommentDTO);
    }

    if (Array.isArray(candidate['data'])) {
      return (candidate['data'] as unknown[])
        .filter((item) => this.isCommentLike(item))
        .map((item) => item as CommentDTO);
    }

    if (Array.isArray(candidate['content'])) {
      return (candidate['content'] as unknown[])
        .filter((item) => this.isCommentLike(item))
        .map((item) => item as CommentDTO);
    }

    if (Array.isArray(candidate['body'])) {
      return (candidate['body'] as unknown[])
        .filter((item) => this.isCommentLike(item))
        .map((item) => item as CommentDTO);
    }

    const values = Object.values(candidate);
    if (values.length && values.every((item) => item && typeof item === 'object')) {
      return values
        .filter((item) => this.isCommentLike(item))
        .map((item) => item as CommentDTO);
    }

    if (this.isCommentLike(candidate)) {
      return [candidate as CommentDTO];
    }

    return [];
  }

  private normalizeText(value: unknown): string {
    if (typeof value === 'string') {
      return value.trim();
    }
    if (typeof value === 'number' || typeof value === 'boolean') {
      return String(value).trim();
    }
    if (typeof value === 'function') {
      return '';
    }
    if (value == null) {
      return '';
    }
    if (typeof value === 'object') {
      const asObject = value as Record<string, unknown>;
      if (typeof asObject['value'] === 'string') {
        return asObject['value'].trim();
      }
      return '';
    }
    return '';
  }

  private safeJsonParse(value: string): unknown | null {
    const text = value.trim();
    if (!text) {
      return null;
    }

    try {
      return JSON.parse(text);
    } catch {
      return null;
    }
  }

  private isCommentLike(value: unknown): value is CommentDTO {
    if (!value || typeof value !== 'object') {
      return false;
    }

    const obj = value as Record<string, unknown>;
    if ('text' in obj) {
      return typeof obj['text'] !== 'function';
    }

    return 'userId' in obj || 'taskId' in obj || 'privateStatus' in obj || 'id' in obj;
  }

  private mapNotification(
    notification: StudentNotificationResponse | null | undefined,
    index: number,
  ): StudentNotification {
    const text = (notification?.text ?? '').trim();
    const fallbackId = `student-notification-${index + 1}`;

    return {
      id: (notification?.id ?? '').trim() || fallbackId,
      type: 'announcement',
      title: text.length > 60 ? `${text.slice(0, 60)}...` : text || RU.notification,
      author: RU.teacher,
      dateTime: this.formatDate(notification?.creationDate),
      text,
      tag: RU.group,
    };
  }

  private formatDate(value: string | undefined): string {
    if (!value) {
      return '';
    }

    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(parsed);
  }

  private mapTaskStatusLabel(status: StudentTaskResponse['taskStatus']): string {
    switch (status) {
      case 'COMPLETE':
        return RU.completed;
      case 'OVERDUE':
        return RU.overdue;
      case 'PENDING':
      default:
        return RU.inProgress;
    }
  }

  private extractAttachmentId(response: unknown): number | null {
    if (typeof response === 'number' && Number.isFinite(response) && response > 0) {
      return response;
    }

    if (response && typeof response === 'object') {
      const candidate = response as Record<string, unknown>;
      const id = candidate['id'] ?? candidate['attachmentId'];
      if (typeof id === 'number' && Number.isFinite(id) && id > 0) {
        return id;
      }
    }

    return null;
  }
}
