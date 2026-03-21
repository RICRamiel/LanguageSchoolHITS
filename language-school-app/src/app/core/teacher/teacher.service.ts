import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  NotificationAttachment,
  TeacherGroup,
  TeacherNotification,
  TeacherTask,
} from './teacher.models';

type TeacherComment = {
  id?: number;
  text?: string;
  userId?: number;
  taskId?: number;
  privateStatus?: boolean;
};

type TeacherTaskResponse = {
  id?: number;
  name?: string;
  description?: string;
  deadline?: string;
  commentList?: TeacherComment[];
  attachmentDownloadInfos?: TeacherAttachmentResponse[] | null;
  taskStatus?: 'COMPLETE' | 'OVERDUE' | 'PENDING';
  groupName?: string;
  teacher?: {
    id?: number;
    firstName?: string;
    lastName?: string;
    email?: string;
    groups?: Array<{
      id?: number;
      name?: string;
    }>;
    role?: 'TEACHER' | 'STUDENT' | 'ADMIN';
  };
};

type TeacherGroupResponse = {
  id?: number;
  name?: string;
};

type TeacherNotificationResponse = {
  id?: string;
  text?: string;
  groupId?: number;
  creationDate?: string;
  attachmentDownloadInfo?: TeacherAttachmentResponse | null;
  attachmentDownloadInfos?: TeacherAttachmentResponse[] | null;
  attachment?: TeacherAttachmentResponse | null;
  attachments?: TeacherAttachmentResponse[] | null;
};

type TeacherAttachmentResponse = {
  id?: number;
  attachmentId?: number;
  fileName?: string;
  name?: string;
  fileType?: string;
  contentType?: string;
  fileSize?: number;
  size?: number;
  objectKey?: string;
};

@Injectable({ providedIn: 'root' })
export class TeacherService {
  private readonly http = inject(HttpClient);

  getGroupsByTeacher(teacherId: number): Observable<TeacherGroup[]> {
    const teacherGroups$ = this.http
      .get<TeacherGroupResponse[]>(withOpenApiBase(OPENAPI_PATHS.teacher.groupsByTeacher(teacherId)))
      .pipe(
        map((groups) => this.normalizeGroups(groups)),
        catchError(() => of([] as TeacherGroup[])),
      );

    const filteredGroups$ = this.http
      .get<TeacherGroupResponse[]>(withOpenApiBase(OPENAPI_PATHS.teacher.groupsWithFilters))
      .pipe(
        map((groups) => this.normalizeGroups(groups)),
        catchError(() => of([] as TeacherGroup[])),
      );

    return forkJoin({ teacherGroups: teacherGroups$, filteredGroups: filteredGroups$ }).pipe(
      map(({ teacherGroups, filteredGroups }) => {
        if (!teacherGroups.length) {
          return [];
        }

        if (!filteredGroups.length) {
          return this.sortGroups(teacherGroups);
        }

        const teacherIds = new Set(teacherGroups.map((group) => group.id));
        const intersection = filteredGroups.filter((group) => teacherIds.has(group.id));
        if (!intersection.length) {
          return this.sortGroups(teacherGroups);
        }

        return this.sortGroups(intersection);
      }),
    );
  }

  getTasksByTeacher(teacherId: number): Observable<TeacherTask[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.teacher.tasksByTeacher(teacherId)))
      .pipe(
        map((tasks) =>
          this.normalizeTaskList(tasks).map((task, index) =>
            this.mapTask(
              task,
              (task.groupName ?? '').trim() || 'Group',
              task.id && Number.isFinite(task.id) ? task.id : -(index + 1),
            ),
          ),
        ),
      );
  }

  getTasksByGroupName(groupName: string): Observable<TeacherTask[]> {
    const normalizedGroupName = groupName.trim();
    if (!normalizedGroupName) {
      return of([]);
    }

    return this.http
      .get<unknown>(
        withOpenApiBase(OPENAPI_PATHS.tasks.byGroupNameReal(normalizedGroupName)),
      )
      .pipe(
        map((tasks) =>
          this.normalizeTaskList(tasks).map((task, index) =>
            this.mapTask(
              task,
              normalizedGroupName,
              task.id && Number.isFinite(task.id) ? task.id : -(index + 1),
            ),
          ),
        ),
      );
  }

  getTaskComments(taskId: number): Observable<TeacherTask['taskComments']> {
    return this.http
      .get<TeacherComment[]>(withOpenApiBase(OPENAPI_PATHS.teacher.commentsByTask(taskId)))
      .pipe(
        map((comments) =>
          (comments ?? []).map((comment) => ({
            studentName: comment.userId ? `User #${comment.userId}` : 'Student',
            text: (comment.text ?? '').trim(),
            createdAt: '',
          })),
        ),
      );
  }

  createComment(
    taskId: number,
    userId: number,
    text: string,
  ): Observable<TeacherTask['taskComments'][number]> {
    return this.http
      .post<TeacherComment>(withOpenApiBase(OPENAPI_PATHS.comments.create), {
        text,
        userId,
        taskId,
        privateStatus: false,
      })
      .pipe(
        map((comment) => ({
          studentName: comment?.userId ? `User #${comment.userId}` : 'Student',
          text: (comment?.text ?? text).trim(),
          createdAt: '',
        })),
      );
  }

  createTask(payload: CreateTaskPayload): Observable<TeacherTask> {
    return this.http
      .post<TeacherTaskResponse>(withOpenApiBase(OPENAPI_PATHS.teacher.createTask), {
        name: payload.title,
        description: payload.description,
        deadline: payload.dueDate,
        groupName: payload.groupName,
      })
      .pipe(
        map((task) => {
          const id = task?.id && Number.isFinite(task.id) ? task.id : Date.now();
          return this.mapTask(task, payload.groupName, id);
        }),
      );
  }

  getNotificationsByGroupIds(groupIds: number[]): Observable<TeacherNotification[]> {
    const uniqueGroupIds = [...new Set(groupIds.filter((groupId) => Number.isFinite(groupId) && groupId > 0))];
    if (!uniqueGroupIds.length) {
      return of([]);
    }

    return forkJoin(
      uniqueGroupIds.map((groupId) =>
        this.http.get<TeacherNotificationResponse[]>(
          withOpenApiBase(OPENAPI_PATHS.notifications.byGroup(groupId)),
        ),
      ),
    ).pipe(
      map((results) =>
        results
          .flat()
          .sort(
            (a, b) =>
              this.parseDateToMillis(b.creationDate ?? '') - this.parseDateToMillis(a.creationDate ?? ''),
          )
          .map((notification) => this.mapNotification(notification)),
      ),
      map((notifications) => {
        const deduplicated = new Map<string, TeacherNotification>();
        notifications.forEach((notification) => deduplicated.set(notification.id, notification));
        return [...deduplicated.values()];
      }),
    );
  }

  createNotification(payload: CreateNotificationPayload): Observable<TeacherNotification> {
    const normalizedTitle = payload.title.trim();
    const normalizedContent = payload.content.trim();
    const composedText = normalizedContent;
    const attachmentFile = payload.attachmentFile ?? null;
    const files$ = attachmentFile ? this.encodeFileToBase64(attachmentFile).pipe(map((f) => [f])) : of([] as string[]);

    return files$.pipe(
      switchMap((files) =>
        this.http.post<TeacherNotificationResponse>(withOpenApiBase(OPENAPI_PATHS.notifications.create), {
          text: composedText,
          groupId: payload.groupId,
          files,
        }),
      ),
      map((notification) =>
        this.mapNotification(
          {
            ...notification,
            text: notification?.text ?? composedText,
            groupId: notification?.groupId ?? payload.groupId,
          },
          normalizedTitle,
        ),
      ),
    );
  }

  attachAttachmentToNotification(
    notificationId: string,
    file: File,
  ): Observable<NotificationAttachment | null> {
    if (!notificationId.trim()) {
      return of(null);
    }

    return this.uploadNotificationAttachment(notificationId, file);
  }

  private uploadNotificationAttachment(
    notificationId: string,
    file: File,
  ): Observable<NotificationAttachment | null> {
    const formData = new FormData();
    formData.append('file', file);
    const params = new HttpParams()
      .set('taskId', notificationId)
      .set('notificationId', notificationId);

    return this.http
      .post<unknown>(withOpenApiBase(OPENAPI_PATHS.attachments.uploadToNotification), formData, {
        params,
      })
      .pipe(
        map((response) => this.mapAttachmentResponse(response, file)),
        catchError(() => of(null)),
      );
  }

  private mapAttachmentResponse(response: unknown, fallbackFile: File): NotificationAttachment | null {
    if (!response || typeof response !== 'object') {
      return {
        id: null,
        objectKey: null,
        fileName: fallbackFile.name || 'attachment',
        fileType: fallbackFile.type || '',
        fileSize: fallbackFile.size,
      };
    }

    const raw = response as Record<string, unknown>;
    const idCandidate = raw['id'] ?? raw['attachmentId'];
    const attachmentId =
      typeof idCandidate === 'number' && Number.isFinite(idCandidate) && idCandidate > 0
        ? idCandidate
        : null;
    const objectKey = this.asTrimmedString(raw['objectKey']) || null;

    const fileName = this.asTrimmedString(raw['fileName'] ?? raw['name']) || fallbackFile.name || 'attachment';
    const fileType = this.asTrimmedString(raw['fileType'] ?? raw['contentType']) || fallbackFile.type || '';
    const sizeCandidate = raw['fileSize'] ?? raw['size'];
    const fileSize =
      typeof sizeCandidate === 'number' && Number.isFinite(sizeCandidate) && sizeCandidate >= 0
        ? sizeCandidate
        : fallbackFile.size;

    return {
      id: attachmentId,
      objectKey,
      fileName,
      fileType,
      fileSize,
    };
  }

  private asTrimmedString(value: unknown): string {
    return typeof value === 'string' ? value.trim() : '';
  }

  private mapNotificationAttachment(
    notification: TeacherNotificationResponse | null | undefined,
  ): NotificationAttachment | null {
    const candidates: Array<TeacherAttachmentResponse | null | undefined> = [
      notification?.attachmentDownloadInfo,
      notification?.attachment,
      ...(notification?.attachmentDownloadInfos ?? []),
      ...(notification?.attachments ?? []),
    ];

    for (const candidate of candidates) {
      if (!candidate) {
        continue;
      }

      const id = this.resolveAttachmentId(candidate.id ?? candidate.attachmentId);
      const objectKey = (candidate.objectKey ?? '').trim() || null;
      const fileName =
        (candidate.fileName ?? candidate.name ?? candidate.objectKey ?? '').trim() || 'attachment';
      const fileType = (candidate.fileType ?? candidate.contentType ?? '').trim();
      const sizeCandidate = candidate.fileSize ?? candidate.size;
      const fileSize =
        typeof sizeCandidate === 'number' && Number.isFinite(sizeCandidate) && sizeCandidate >= 0
          ? sizeCandidate
          : null;

      if (id || fileName) {
        return {
          id,
          objectKey,
          fileName,
          fileType,
          fileSize,
        };
      }
    }

    return null;
  }

  private resolveAttachmentId(value: unknown): number | null {
    return typeof value === 'number' && Number.isFinite(value) && value > 0 ? value : null;
  }

  private normalizeGroups(groups: TeacherGroupResponse[] | null | undefined): TeacherGroup[] {
    return (groups ?? [])
      .map((group) => ({
        id: Number(group.id),
        name: (group.name ?? '').trim(),
      }))
      .filter((group) => Number.isFinite(group.id) && group.id > 0 && Boolean(group.name));
  }

  private sortGroups(groups: TeacherGroup[]): TeacherGroup[] {
    return [...groups].sort((a, b) => a.name.localeCompare(b.name, 'ru'));
  }

  private normalizeTaskList(payload: unknown): TeacherTaskResponse[] {
    if (typeof payload === 'string') {
      const parsed = this.safeJsonParse(payload);
      return parsed === null ? [] : this.normalizeTaskList(parsed);
    }

    if (Array.isArray(payload)) {
      return payload.filter((item) => this.isTaskLike(item)) as TeacherTaskResponse[];
    }

    if (!payload || typeof payload !== 'object') {
      return [];
    }

    const candidate = payload as Record<string, unknown>;
    const possibleKeys = ['tasks', 'data', 'content', 'body'] as const;
    for (const key of possibleKeys) {
      if (Array.isArray(candidate[key])) {
        return (candidate[key] as unknown[]).filter((item) => this.isTaskLike(item)) as TeacherTaskResponse[];
      }
    }

    if (this.isTaskLike(candidate)) {
      return [candidate as TeacherTaskResponse];
    }

    return [];
  }

  private isTaskLike(value: unknown): value is TeacherTaskResponse {
    if (!value || typeof value !== 'object') {
      return false;
    }
    const obj = value as Record<string, unknown>;
    return 'name' in obj || 'description' in obj || 'deadline' in obj || 'taskStatus' in obj || 'teacher' in obj;
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

  private encodeFileToBase64(file: File): Observable<string> {
    return new Observable<string>((observer) => {
      const reader = new FileReader();

      reader.onload = () => {
        const result = reader.result;
        if (typeof result !== 'string') {
          observer.error(new Error('Failed to read file'));
          return;
        }

        const base64 = result.includes(',') ? result.split(',')[1] : result;
        observer.next(base64);
        observer.complete();
      };

      reader.onerror = () => {
        observer.error(new Error('Failed to read file'));
      };

      reader.readAsDataURL(file);
    });
  }

  private mapTask(task: TeacherTaskResponse | null | undefined, groupName: string, id: number): TeacherTask {
    const commentCount = task?.commentList?.length ?? 0;
    const attachedWorks = this.mapTaskAttachments(task);
    const responseGroupName = (task?.groupName ?? '').trim();
    const teacherGroupName = (task?.teacher?.groups?.[0]?.name ?? '').trim();
    const resolvedGroupName = responseGroupName || teacherGroupName || groupName || 'Group';
    const teacherName = [task?.teacher?.lastName, task?.teacher?.firstName].filter(Boolean).join(' ').trim();
    return {
      id,
      title: (task?.name ?? '').trim() || `Task #${id}`,
      description: (task?.description ?? '').trim(),
      dueDate: this.formatDate(task?.deadline),
      status: task?.taskStatus ?? 'PENDING',
      teacherName: teacherName || 'Teacher',
      submissions: `${attachedWorks.length} files`,
      comments: `${commentCount} comments`,
      group: resolvedGroupName,
      attachedWorks,
      taskComments: [],
    };
  }

  private mapTaskAttachments(task: TeacherTaskResponse | null | undefined): TeacherTask['attachedWorks'] {
    return (task?.attachmentDownloadInfos ?? [])
      .map((attachment) => {
        const id = this.resolveAttachmentId(attachment?.id ?? attachment?.attachmentId);
        const objectKey = (attachment?.objectKey ?? '').trim() || null;
        const fileName =
          (attachment?.fileName ?? attachment?.name ?? attachment?.objectKey ?? '').trim() || 'attachment';
        const fileType = (attachment?.fileType ?? attachment?.contentType ?? '').trim();
        const sizeCandidate = attachment?.fileSize ?? attachment?.size;
        const fileSize =
          typeof sizeCandidate === 'number' && Number.isFinite(sizeCandidate) && sizeCandidate >= 0
            ? sizeCandidate
            : null;

        return {
          id,
          fileName,
          fileType,
          fileSize,
          objectKey,
        };
      })
      .filter((attachment) => Boolean(attachment.fileName));
  }

  private mapNotification(
    notification: TeacherNotificationResponse | null | undefined,
    titleOverride?: string,
  ): TeacherNotification {
    const id = (notification?.id ?? '').trim() || `notification-${Date.now()}-${Math.random()}`;
    const text = (notification?.text ?? '').trim();
    return {
      id,
      title: titleOverride?.trim() || this.getTitleFromText(text),
      text,
      date: this.formatDate(notification?.creationDate),
      groupId: notification?.groupId ?? 0,
      attachment: this.mapNotificationAttachment(notification),
    };
  }

  private getTitleFromText(text: string): string {
    const normalized = text.trim();
    if (!normalized) {
      return 'Notification';
    }
    return normalized.length > 50 ? `${normalized.slice(0, 50)}...` : normalized;
  }

  private formatDate(value: string | undefined): string {
    if (!value) {
      return '';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return new Intl.DateTimeFormat('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(date);
  }

  private parseDateToMillis(value: string): number {
    if (!value) {
      return 0;
    }
    const parsed = Date.parse(value);
    return Number.isNaN(parsed) ? 0 : parsed;
  }
}
