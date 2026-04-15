import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  NotificationAttachment,
  TaskAssignmentType,
  TaskTeam,
  TeacherGroup,
  TeacherNotification,
  TeacherStudentGrade,
  TeacherTask,
} from './teacher.models';

type TeacherComment = {
  id?: number | string;
  text?: string;
  userId?: number | string;
  taskId?: number | string;
  privateStatus?: boolean;
};

type TeacherTeamResponse = {
  id?: string;
  name?: string;
  membersCount?: number | null;
  captainId?: string | null;
};

type TeacherTaskResponse = {
  id?: number | string;
  name?: string;
  description?: string;
  deadline?: string;
  courseId?: number | string;
  courseName?: string;
  teamType?: TeacherTask['teamType'];
  resolveType?: TeacherTask['resolveType'];
  maxTeamSize?: number | null;
  minTeamSize?: number | null;
  maxTeamsAmount?: number | null;
  minTeamsAmount?: number | null;
  votesThreshold?: number | null;
  commentList?: TeacherComment[];
  attachmentDownloadInfos?: TeacherAttachmentResponse[] | null;
  taskStatus?: 'COMPLETE' | 'OVERDUE' | 'PENDING';
  groupName?: string;
  teams?: TeacherTeamResponse[] | null;
  teacher?: {
    id?: number | string;
    firstName?: string;
    lastName?: string;
    email?: string;
    groups?: Array<{
      id?: number | string;
      name?: string;
    }>;
    role?: 'TEACHER' | 'STUDENT' | 'ADMIN';
  };
};

type TeacherGroupResponse = {
  id?: number | string;
  name?: string;
};

type TeacherStudentResponse = {
  id?: number | string;
  firstName?: string;
  lastName?: string;
  email?: string;
};

type TeacherNotificationResponse = {
  id?: string;
  text?: string;
  groupId?: number | string;
  courseId?: number | string;
  creationDate?: string;
  attachmentDownloadInfo?: TeacherAttachmentResponse | null;
  attachmentDownloadInfos?: TeacherAttachmentResponse[] | null;
  attachment?: TeacherAttachmentResponse | null;
  attachments?: TeacherAttachmentResponse[] | null;
};

type TeacherAttachmentResponse = {
  id?: number | string;
  attachmentId?: number | string;
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

  getStudentsByGroup(groupId: string): Observable<TeacherStudentGrade[]> {
    if (!String(groupId).trim()) {
      return of([]);
    }

    return this.http
      .get<TeacherStudentResponse[]>(withOpenApiBase(OPENAPI_PATHS.admin.students.listByGroup(groupId)))
      .pipe(
        map((students) =>
          (students ?? [])
            .map((student) => {
              const id = String(student.id ?? '').trim();
              if (!id) {
                return null;
              }

              const firstName = (student.firstName ?? '').trim();
              const lastName = (student.lastName ?? '').trim();
              const fullName = [lastName, firstName].filter(Boolean).join(' ').trim() || `Студент #${id}`;

              return {
                id,
                firstName,
                lastName,
                fullName,
                email: (student.email ?? '').trim(),
                grade: '',
                saving: false,
                error: null,
              } as TeacherStudentGrade;
            })
            .filter((student): student is TeacherStudentGrade => student !== null),
        ),
      );
  }

  updateStudentGrade(student: TeacherStudentGrade): Observable<void> {
    const normalizedGrade = student.grade.trim();

    return this.http
      .put(
        withOpenApiBase(OPENAPI_PATHS.admin.students.update(student.id)),
        {
          firstName: student.firstName,
          lastName: student.lastName,
          grade: normalizedGrade,
        },
      )
      .pipe(map(() => void 0));
  }

  getGroupsByTeacher(teacherId: string | number): Observable<TeacherGroup[]> {
    return this.http
      .get<TeacherGroupResponse[]>(withOpenApiBase(OPENAPI_PATHS.courses.list))
      .pipe(
        map((groups) => this.normalizeGroups(groups)),
        map((groups) => this.sortGroups(groups)),
        catchError(() => of([] as TeacherGroup[])),
      );
  }

  getTasksByTeacher(teacherId: string | number): Observable<TeacherTask[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.teacher.tasksByTeacher(teacherId)))
      .pipe(
        map((tasks) =>
          this.normalizeTaskList(tasks).map((task, index) =>
            this.mapTask(
              task,
              (task.groupName ?? '').trim() || 'Курс',
              String(task.id ?? `-${index + 1}`),
            ),
          ),
        ),
      );
  }

  getTasksByCourseId(courseId: number | string): Observable<TeacherTask[]> {
    const normalizedCourseId = String(courseId).trim();
    if (!normalizedCourseId) {
      return of([]);
    }

    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.tasks.byCourse(normalizedCourseId)))
      .pipe(
        map((tasks) =>
          this.normalizeTaskList(tasks).map((task, index) =>
            this.mapTask(
              task,
              (task.courseName ?? task.groupName ?? '').trim(),
              String(task.id ?? `-${index + 1}`),
            ),
          ),
        ),
      );
  }

  getTaskComments(taskId: string): Observable<TeacherTask['taskComments']> {
    return this.http
      .get<TeacherComment[]>(withOpenApiBase(OPENAPI_PATHS.teacher.commentsByTask(taskId)))
      .pipe(
        map((comments) =>
          (comments ?? []).map((comment) => ({
            studentName: comment.userId ? `Пользователь #${comment.userId}` : 'Студент',
            text: (comment.text ?? '').trim(),
            createdAt: '',
          })),
        ),
      );
  }

  createComment(
    taskId: string,
    userId: string,
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
          studentName: comment?.userId ? `Пользователь #${comment.userId}` : 'Студент',
          text: (comment?.text ?? text).trim(),
          createdAt: '',
        })),
      );
  }

  createTeam(taskId: string, name: string): Observable<TaskTeam> {
    return this.http
      .post<TeacherTeamResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.teams(taskId)), { name })
      .pipe(map((team) => this.mapTeam(team)));
  }

  addStudentToTeam(taskId: string, teamId: string, studentId: string): Observable<TaskTeam> {
    return this.http
      .post<TeacherTeamResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.addStudentToTeam(taskId, teamId, studentId)),
        {},
      )
      .pipe(map((team) => this.mapTeam(team)));
  }

  createTask(payload: CreateTaskPayload): Observable<TeacherTask> {
    const isTeamTask = payload.assignmentType === 'TEAM';

    return this.http
      .post<TeacherTaskResponse>(withOpenApiBase(OPENAPI_PATHS.teacher.createTask), {
        name: payload.title,
        description: payload.description,
        deadline: payload.dueDate,
        courseId: payload.groupId,
        courseName: payload.groupName,
        teamType: payload.teamType === 'CUSTOM' ? 'FREEROAM' : payload.teamType,
        resolveType: payload.resolveType,
        maxTeamSize: isTeamTask ? payload.maxTeamSize : null,
        minTeamSize: isTeamTask ? payload.minTeamSize : null,
        maxTeamsAmount: isTeamTask ? payload.maxTeamsAmount : null,
        minTeamsAmount: isTeamTask ? payload.minTeamsAmount : null,
        votesThreshold: payload.resolveType === 'AT_LEAST_VOTES_SOLUTION' ? payload.votesThreshold : null,
        teamsCreationTimeout: isTeamTask && payload.teamType === 'DRAFT' ? payload.teamsCreationTimeout : null,
      })
      .pipe(
        map((task) => {
          const id = String(task?.id ?? Date.now());
          return this.mapTask(task, payload.groupName, id, payload.assignmentType);
        }),
      );
  }

  getNotificationsByGroupIds(groupIds: string[]): Observable<TeacherNotification[]> {
    const uniqueGroupIds = [...new Set(groupIds.map((groupId) => String(groupId).trim()).filter(Boolean))];
    if (!uniqueGroupIds.length) {
      return of([]);
    }

    return forkJoin(
      uniqueGroupIds.map((groupId) =>
        this.http.get<TeacherNotificationResponse[]>(
          withOpenApiBase(OPENAPI_PATHS.notifications.byCourse(groupId)),
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
          courseId: payload.groupId,
          files,
        }),
      ),
      map((notification) =>
        this.mapNotification(
          {
            ...notification,
            text: notification?.text ?? composedText,
            courseId: notification?.courseId ?? payload.groupId,
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
        fileName: fallbackFile.name || 'вложение',
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

    const fileName = this.asTrimmedString(raw['fileName'] ?? raw['name']) || fallbackFile.name || 'вложение';
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
        (candidate.fileName ?? candidate.name ?? candidate.objectKey ?? '').trim() || 'вложение';
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

  private resolveAttachmentId(value: unknown): string | null {
    if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
      return String(value);
    }
    if (typeof value === 'string') {
      const normalized = value.trim();
      return normalized || null;
    }
    return null;
  }

  private normalizeGroups(groups: TeacherGroupResponse[] | null | undefined): TeacherGroup[] {
    return (groups ?? [])
      .map((group) => ({
        id: String(group.id ?? '').trim(),
        name: (group.name ?? '').trim(),
      }))
      .filter((group) => Boolean(group.id) && Boolean(group.name));
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
          observer.error(new Error('Не удалось прочитать файл'));
          return;
        }

        const base64 = result.includes(',') ? result.split(',')[1] : result;
        observer.next(base64);
        observer.complete();
      };

      reader.onerror = () => {
        observer.error(new Error('Не удалось прочитать файл'));
      };

      reader.readAsDataURL(file);
    });
  }

  private mapTask(
    task: TeacherTaskResponse | null | undefined,
    groupName: string,
    id: string,
    fallbackAssignmentType: TaskAssignmentType = 'TEAM',
  ): TeacherTask {
    const minTeamSize = this.normalizeNullableNumber(task?.minTeamSize);
    const maxTeamSize = this.normalizeNullableNumber(task?.maxTeamSize);
    const minTeamsAmount = this.normalizeNullableNumber(task?.minTeamsAmount);
    const maxTeamsAmount = this.normalizeNullableNumber(task?.maxTeamsAmount);
    const hasTeamBounds =
      minTeamSize !== null || maxTeamSize !== null || minTeamsAmount !== null || maxTeamsAmount !== null;
    const assignmentType: TaskAssignmentType = hasTeamBounds ? 'TEAM' : fallbackAssignmentType;
    const teamType = (task?.teamType ?? 'FREEROAM') as TeacherTask['teamType'];
    const resolveType = (task?.resolveType ?? 'LAST_SUBMITTED_SOLUTION') as TeacherTask['resolveType'];
    const votesThreshold = this.normalizeNullableNumber(task?.votesThreshold);
    const commentCount = task?.commentList?.length ?? 0;
    const attachedWorks = this.mapTaskAttachments(task);
    const responseGroupName = (task?.courseName ?? task?.groupName ?? '').trim();
    const teacherGroupName = (task?.teacher?.groups?.[0]?.name ?? '').trim();
    const resolvedGroupName = responseGroupName || teacherGroupName || groupName || 'Курс';
    const teacherName = [task?.teacher?.lastName, task?.teacher?.firstName].filter(Boolean).join(' ').trim();
    return {
      id,
      title: (task?.name ?? '').trim() || `Задание ${id}`,
      description: (task?.description ?? '').trim(),
      dueDate: this.formatDate(task?.deadline),
      status: task?.taskStatus ?? 'PENDING',
      teacherName: teacherName || 'Преподаватель',
      submissions: `${attachedWorks.length} файлов`,
      comments: `${commentCount} комментариев`,
      group: resolvedGroupName,
      attachedWorks,
      taskComments: [],
      assignmentType,
      teamType,
      resolveType,
      minTeamSize,
      maxTeamSize,
      minTeamsAmount,
      maxTeamsAmount,
      votesThreshold,
      teams: (task?.teams ?? []).map((t) => this.mapTeam(t)),
    };
  }

  private mapTeam(team: TeacherTeamResponse | null | undefined): TaskTeam {
    return {
      id: (team?.id ?? '').trim(),
      name: (team?.name ?? '').trim() || 'Команда',
      membersCount: typeof team?.membersCount === 'number' ? team.membersCount : null,
      captainId: (team?.captainId ?? null) ? String(team!.captainId) : null,
    };
  }

  private normalizeNullableNumber(value: unknown): number | null {
    return typeof value === 'number' && Number.isFinite(value) ? value : null;
  }

  private mapTaskAttachments(task: TeacherTaskResponse | null | undefined): TeacherTask['attachedWorks'] {
    return (task?.attachmentDownloadInfos ?? [])
      .map((attachment) => {
        const id = this.resolveAttachmentId(attachment?.id ?? attachment?.attachmentId);
        const objectKey = (attachment?.objectKey ?? '').trim() || null;
        const fileName =
          (attachment?.fileName ?? attachment?.name ?? attachment?.objectKey ?? '').trim() || 'вложение';
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
      groupId: String(notification?.courseId ?? notification?.groupId ?? ''),
      attachment: this.mapNotificationAttachment(notification),
    };
  }

  private getTitleFromText(text: string): string {
    const normalized = text.trim();
    if (!normalized) {
      return 'Уведомление';
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
