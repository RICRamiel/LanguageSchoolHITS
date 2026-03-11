import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { forkJoin, map, Observable, of } from 'rxjs';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  TeacherGroup,
  TeacherNotification,
  TeacherTask,
} from './teacher.models';

type TeacherComment = {
  text?: string;
  userId?: number;
};

type TeacherTaskResponse = {
  id?: number;
  name?: string;
  description?: string;
  deadline?: string;
  commentList?: TeacherComment[];
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
};

@Injectable({ providedIn: 'root' })
export class TeacherService {
  private readonly http = inject(HttpClient);

  getGroupsByTeacher(teacherId: number): Observable<TeacherGroup[]> {
    return this.http
      .get<TeacherGroupResponse[]>(withOpenApiBase(OPENAPI_PATHS.teacher.groupsByTeacher(teacherId)))
      .pipe(
        map((groups) =>
          (groups ?? [])
            .map((group) => ({
              id: Number(group.id),
              name: (group.name ?? '').trim(),
            }))
            .filter((group) => Number.isFinite(group.id) && group.id > 0 && Boolean(group.name)),
        ),
      );
  }

  getTasksByTeacher(teacherId: number): Observable<TeacherTask[]> {
    return this.http
      .get<TeacherTaskResponse[]>(withOpenApiBase(OPENAPI_PATHS.teacher.tasksByTeacher(teacherId)))
      .pipe(
        map((tasks) =>
          (tasks ?? []).map((task, index) =>
            this.mapTask(
              task,
              'Group',
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
          withOpenApiBase(OPENAPI_PATHS.teacher.notificationsByGroup(groupId)),
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
    return this.http
      .post<TeacherNotificationResponse>(withOpenApiBase(OPENAPI_PATHS.teacher.createNotification), {
        text: payload.content,
        groupId: payload.groupId,
      })
      .pipe(
        map((notification) =>
          this.mapNotification(
            {
              ...notification,
              text: notification?.text ?? payload.content,
              groupId: notification?.groupId ?? payload.groupId,
            },
            payload.title,
          ),
        ),
      );
  }

  private mapTask(task: TeacherTaskResponse | null | undefined, groupName: string, id: number): TeacherTask {
    const commentCount = task?.commentList?.length ?? 0;
    return {
      id,
      title: (task?.name ?? '').trim() || `Task #${id}`,
      description: (task?.description ?? '').trim(),
      dueDate: this.formatDate(task?.deadline),
      submissions: '0 submissions',
      comments: `${commentCount} comments`,
      group: groupName || 'Group',
      attachedWorks: [],
      taskComments: [],
    };
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
