import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';
import {
  AssessmentDetails,
  AssessmentSubmitItem,
  CreateNotificationPayload,
  CreateTaskPayload,
  NotificationAttachment,
  ParticipationAssessment,
  ParticipationAssessmentItem,
  AssessmentType,
  PeerReviewAssignment,
  PeerReviewDistributionType,
  PeerReviewEnablePayload,
  PeerReviewManualAssignmentPayload,
  PeerReviewSettings,
  PeerReviewWithoutReviewerWarning,
  PeerAssessmentCriterionResult,
  PeerAssessmentEditItem,
  PeerAssessmentResult,
  TaskCriterion,
  TaskCriterionPayload,
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
  participations?: Array<{
    id?: string;
    studentId?: string;
    studentName?: string;
    mark?: number | null;
    attachments?: TeacherAttachmentResponse[] | null;
  }> | null;
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
  peerReviewEnabled?: boolean | null;
  peerReviewDistributionType?: string | null;
  peerReviewerVisibleToTeams?: boolean | null;
  peerReviewConfirmedAt?: string | null;
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

type TeacherCourseResponse = {
  id?: number | string;
  name?: string;
  teacher?: {
    id?: number | string;
  } | null;
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

type TaskCriterionResponse = {
  id?: string;
  taskId?: string;
  title?: string;
  description?: string;
  maxPoints?: number;
  sectionName?: string;
  orderIndex?: number;
  active?: boolean;
};

type AssessmentDetailsResponse = {
  id?: string;
  type?: AssessmentType;
  totalPoints?: number;
  totalMaxPoints?: number;
  updatedAt?: string | null;
  items?: ParticipationAssessmentItemResponse[] | null;
};

type ParticipationAssessmentItemResponse = {
  criterionId?: string;
  title?: string;
  description?: string;
  maxPoints?: number;
  sectionName?: string;
  orderIndex?: number;
  active?: boolean;
  points?: number | null;
  comment?: string | null;
  teacherPoints?: number | null;
  selfPoints?: number | null;
  teacherComment?: string | null;
  selfComment?: string | null;
};

type ParticipationAssessmentResponse = {
  taskId?: string;
  participationId?: string;
  totalMaxPoints?: number;
  teacherTotal?: number | null;
  selfTotal?: number | null;
  criteria?: ParticipationAssessmentItemResponse[] | null;
  teacherAssessment?: AssessmentDetailsResponse | null;
  selfAssessment?: AssessmentDetailsResponse | null;
};

type PeerReviewAssignmentResponse = {
  id?: string;
  taskId?: string;
  reviewerTeamId?: string;
  reviewedTeamId?: string;
  targetParticipationId?: string;
  assessmentId?: string;
  status?: string;
  createdAt?: string | null;
  submittedAt?: string | null;
  teacherEditorId?: string | null;
  teacherEditedAt?: string | null;
};

type PeerReviewWithoutReviewerWarningResponse = {
  assignmentId?: string;
  teamId?: string;
  teamName?: string;
  message?: string;
};

type PeerAssessmentResultResponse = {
  taskId?: string;
  assignment?: PeerReviewAssignmentResponse | null;
  assessment?: AssessmentDetailsResponse | null;
  reviewerTeamId?: string;
  reviewerTeamName?: string;
  reviewedTeamId?: string;
  reviewedTeamName?: string;
  targetParticipationId?: string;
  status?: string;
};

type PeerReviewResultsResponse = {
  taskId?: string;
  peerReviewEnabled?: boolean;
  peerReviewDistributionType?: string;
  peerReviewerVisibleToTeams?: boolean;
  peerReviewConfirmedAt?: string | null;
  totalMaxPoints?: number | null;
  results?: PeerAssessmentResultResponse[] | null;
};

type PeerReviewSettingsResponse = {
  taskId?: string;
  peerReviewEnabled?: boolean;
  peerReviewDistributionType?: string | null;
  peerReviewerVisibleToTeams?: boolean;
  peerReviewConfirmedAt?: string | null;
  hasTeamsWithoutReviewer?: boolean;
  assignments?: PeerReviewAssignmentResponse[] | null;
  teamsWithoutReviewer?: PeerReviewWithoutReviewerWarningResponse[] | null;
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

  getTaskCriteria(taskId: string): Observable<TaskCriterion[]> {
    const normalizedTaskId = taskId.trim();
    if (!normalizedTaskId) {
      return of([]);
    }

    return this.http
      .get<TaskCriterionResponse[]>(withOpenApiBase(OPENAPI_PATHS.tasks.criteria(normalizedTaskId)))
      .pipe(
        map((criteria) => (criteria ?? []).map((item) => this.mapTaskCriterion(item, normalizedTaskId))),
        map((criteria) => criteria.sort((a, b) => a.orderIndex - b.orderIndex)),
      );
  }

  createTaskCriterion(taskId: string, payload: TaskCriterionPayload): Observable<TaskCriterion> {
    return this.http
      .post<TaskCriterionResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.criteria(taskId.trim())), payload)
      .pipe(map((criterion) => this.mapTaskCriterion(criterion, taskId.trim())));
  }

  updateTaskCriterion(taskId: string, criterionId: string, payload: TaskCriterionPayload): Observable<TaskCriterion> {
    return this.http
      .put<TaskCriterionResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.criterionById(taskId.trim(), criterionId.trim())),
        payload,
      )
      .pipe(map((criterion) => this.mapTaskCriterion(criterion, taskId.trim())));
  }

  deactivateTaskCriterion(taskId: string, criterionId: string): Observable<void> {
    return this.http
      .delete<void>(withOpenApiBase(OPENAPI_PATHS.tasks.criterionById(taskId.trim(), criterionId.trim())))
      .pipe(map(() => void 0));
  }

  getParticipationAssessment(taskId: string, participationId: string): Observable<ParticipationAssessment> {
    return this.http
      .get<ParticipationAssessmentResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.assessment(taskId.trim(), participationId.trim())),
      )
      .pipe(map((assessment) => this.mapParticipationAssessment(assessment, taskId.trim(), participationId.trim())));
  }

  submitTeacherAssessment(
    taskId: string,
    participationId: string,
    items: AssessmentSubmitItem[],
  ): Observable<AssessmentDetails> {
    return this.http
      .put<AssessmentDetailsResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.teacherAssessment(taskId.trim(), participationId.trim())),
        { items },
      )
      .pipe(map((assessment) => this.mapAssessmentDetails(assessment)));
  }

  submitSelfAssessment(
    taskId: string,
    participationId: string,
    items: AssessmentSubmitItem[],
  ): Observable<AssessmentDetails> {
    return this.http
      .put<AssessmentDetailsResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.selfAssessment(taskId.trim(), participationId.trim())),
        { items },
      )
      .pipe(map((assessment) => this.mapAssessmentDetails(assessment)));
  }

  getPeerAssessmentResults(taskId: string): Observable<PeerAssessmentResult[]> {
    const normalizedTaskId = taskId.trim();
    if (!normalizedTaskId) {
      return of([]);
    }

    return this.http
      .get<unknown>(
        withOpenApiBase(OPENAPI_PATHS.tasks.peerReviewResults(normalizedTaskId)),
      )
      .pipe(
        map((response) => this.normalizePeerReviewResults(response)),
      );
  }

  getPeerReviewSettings(taskId: string): Observable<PeerReviewSettings> {
    const normalizedTaskId = taskId.trim();
    if (!normalizedTaskId) {
      return of(this.emptyPeerReviewSettings(''));
    }

    return this.http
      .get<PeerReviewSettingsResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.peerReviewSettings(normalizedTaskId)))
      .pipe(map((settings) => this.mapPeerReviewSettings(settings, normalizedTaskId)));
  }

  enablePeerReview(taskId: string, payload: PeerReviewEnablePayload): Observable<TeacherTask | null> {
    const normalizedTaskId = taskId.trim();
    return this.http
      .post<TeacherTaskResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.enablePeerReview(normalizedTaskId)),
        payload,
      )
      .pipe(map((task) => task ? this.mapTask(task, task.courseName ?? task.groupName ?? '', normalizedTaskId) : null));
  }

  assignManualPeerReview(
    taskId: string,
    payload: PeerReviewManualAssignmentPayload,
  ): Observable<PeerReviewAssignment> {
    const normalizedTaskId = taskId.trim();
    return this.http
      .post<PeerReviewAssignmentResponse>(
        withOpenApiBase(OPENAPI_PATHS.tasks.manualPeerReviewAssignments(normalizedTaskId)),
        payload,
      )
      .pipe(map((assignment) => this.mapPeerReviewAssignment(assignment, normalizedTaskId)));
  }

  confirmPeerReview(taskId: string): Observable<PeerAssessmentResult[]> {
    const normalizedTaskId = taskId.trim();
    return this.http
      .post<PeerReviewResultsResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.confirmPeerReview(normalizedTaskId)), null)
      .pipe(map((response) => this.normalizePeerReviewResults(response)));
  }

  editPeerAssessment(taskId: string, assignmentId: string, items: PeerAssessmentEditItem[]): Observable<void> {
    return this.http
      .put<void>(
        withOpenApiBase(OPENAPI_PATHS.tasks.peerReviewAssessment(taskId.trim(), assignmentId.trim())),
        { items },
      )
      .pipe(map(() => void 0));
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
    const normalizedTeacherId = this.toId(teacherId);
    return this.http
      .get<TeacherCourseResponse[]>(withOpenApiBase(OPENAPI_PATHS.courses.list))
      .pipe(
        map((courses) =>
          this.normalizeGroups(
            (courses ?? []).filter((course) => {
              if (!normalizedTeacherId) {
                return true;
              }
              return this.toId(course?.teacher?.id) === normalizedTeacherId;
            }),
          ),
        ),
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
    const teamType = this.normalizeTeamTypeForApi(payload.teamType);

    return this.http
      .post<TeacherTaskResponse>(withOpenApiBase(OPENAPI_PATHS.teacher.createTask), {
        name: payload.title,
        description: payload.description,
        deadline: payload.dueDate,
        courseId: payload.groupId,
        courseName: payload.groupName,
        teamType,
        resolveType: payload.resolveType,
        maxTeamSize: isTeamTask ? payload.maxTeamSize : null,
        minTeamSize: isTeamTask ? payload.minTeamSize : null,
        maxTeamsAmount: isTeamTask ? payload.maxTeamsAmount : null,
        minTeamsAmount: isTeamTask ? payload.minTeamsAmount : null,
        votesThreshold:
          payload.resolveType === 'AT_LEAST_VOTES_SOLUTION' || payload.resolveType === 'MOST_VOTES_SOLUTION'
            ? payload.votesThreshold
            : null,
        teamsCreationTimeout: isTeamTask && teamType === 'DRAFT' ? payload.teamsCreationTimeout : null,
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

  private toId(value: unknown): string {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value);
    }
    return this.asTrimmedString(value);
  }

  private resolveFiniteNumber(value: unknown): number | null {
    return typeof value === 'number' && Number.isFinite(value) ? value : null;
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

  private mapTaskCriterion(item: TaskCriterionResponse | null | undefined, taskId: string): TaskCriterion {
    const maxPoints =
      typeof item?.maxPoints === 'number' && Number.isFinite(item.maxPoints) ? item.maxPoints : 0;
    const orderIndex =
      typeof item?.orderIndex === 'number' && Number.isFinite(item.orderIndex) ? item.orderIndex : 0;
    return {
      id: (item?.id ?? '').trim(),
      taskId: (item?.taskId ?? taskId).trim(),
      title: (item?.title ?? '').trim(),
      description: (item?.description ?? '').trim(),
      maxPoints,
      sectionName: (item?.sectionName ?? '').trim(),
      orderIndex,
      active: item?.active ?? true,
    };
  }

  private mapParticipationAssessment(
    response: ParticipationAssessmentResponse | null | undefined,
    taskId: string,
    participationId: string,
  ): ParticipationAssessment {
    return {
      taskId: (response?.taskId ?? taskId).trim(),
      participationId: (response?.participationId ?? participationId).trim(),
      totalMaxPoints:
        typeof response?.totalMaxPoints === 'number' && Number.isFinite(response.totalMaxPoints)
          ? response.totalMaxPoints
          : 0,
      teacherTotal:
        typeof response?.teacherTotal === 'number' && Number.isFinite(response.teacherTotal)
          ? response.teacherTotal
          : null,
      selfTotal:
        typeof response?.selfTotal === 'number' && Number.isFinite(response.selfTotal)
          ? response.selfTotal
          : null,
      criteria: (response?.criteria ?? []).map((item) => this.mapParticipationAssessmentItem(item)),
      teacherAssessment: response?.teacherAssessment ? this.mapAssessmentDetails(response.teacherAssessment) : null,
      selfAssessment: response?.selfAssessment ? this.mapAssessmentDetails(response.selfAssessment) : null,
    };
  }

  private mapAssessmentDetails(item: AssessmentDetailsResponse | null | undefined): AssessmentDetails {
    return {
      id: (item?.id ?? '').trim(),
      type: item?.type === 'SELF' ? 'SELF' : item?.type === 'PEER' ? 'PEER' : 'TEACHER',
      totalPoints: typeof item?.totalPoints === 'number' && Number.isFinite(item.totalPoints) ? item.totalPoints : 0,
      totalMaxPoints:
        typeof item?.totalMaxPoints === 'number' && Number.isFinite(item.totalMaxPoints) ? item.totalMaxPoints : 0,
      items: (item?.items ?? []).map((entry) => this.mapParticipationAssessmentItem(entry)),
    };
  }

  private mapPeerAssessmentResult(
    result: PeerAssessmentResultResponse | null | undefined,
    fallbackTotalMaxPoints: number | null = null,
  ): PeerAssessmentResult {
    const assessment = result?.assessment ?? null;
    const assignment = result?.assignment ?? null;
    const criteria = (assessment?.items ?? [])
      .map((item) => this.mapPeerAssessmentCriterionResult(item))
      .sort((a, b) => a.orderIndex - b.orderIndex);
    const totalMaxPoints = this.resolveFiniteNumber(assessment?.totalMaxPoints)
      ?? fallbackTotalMaxPoints
      ?? criteria.reduce((total, criterion) => total + criterion.maxPoints, 0);
    const totalPoints = this.resolveFiniteNumber(assessment?.totalPoints)
      ?? (criteria.every((criterion) => criterion.points !== null)
        ? criteria.reduce((total, criterion) => total + (criterion.points ?? 0), 0)
        : null);

    return {
      id: this.toId(assignment?.id) || this.toId(assessment?.id) || `peer-result-${Math.random()}`,
      assignmentId: this.toId(assignment?.id) || null,
      assessmentId: this.toId(assessment?.id) || null,
      reviewedTeamId: this.toId(assignment?.reviewedTeamId ?? result?.reviewedTeamId),
      reviewedTeamName:
        this.asTrimmedString(result?.reviewedTeamName)
        || 'Команда не указана',
      reviewerTeamId: this.toId(assignment?.reviewerTeamId ?? result?.reviewerTeamId),
      reviewerTeamName:
        this.asTrimmedString(result?.reviewerTeamName)
        || 'Команда-оценщик не указана',
      status: this.asTrimmedString(assignment?.status ?? result?.status) || 'SUBMITTED',
      submittedAt: this.asTrimmedString(assignment?.submittedAt) || this.asTrimmedString(assessment?.updatedAt) || null,
      totalPoints,
      totalMaxPoints,
      criteria,
    };
  }

  private normalizePeerReviewResults(payload: unknown): PeerAssessmentResult[] {
    if (Array.isArray(payload)) {
      return payload.map((item) => this.mapPeerAssessmentResult(item as PeerAssessmentResultResponse));
    }

    if (!payload || typeof payload !== 'object') {
      return [];
    }

    const record = payload as PeerReviewResultsResponse & Record<string, unknown>;
    const totalMaxPoints = this.resolveFiniteNumber(record.totalMaxPoints);
    const results = Array.isArray(record.results)
      ? record.results
      : Array.isArray(record['result'])
        ? (record['result'] as PeerAssessmentResultResponse[])
        : Array.isArray(record['data'])
          ? (record['data'] as PeerAssessmentResultResponse[])
          : [];

    return results.map((item) => this.mapPeerAssessmentResult(item, totalMaxPoints));
  }

  private mapPeerReviewSettings(
    settings: PeerReviewSettingsResponse | null | undefined,
    fallbackTaskId: string,
  ): PeerReviewSettings {
    const taskId = this.toId(settings?.taskId) || fallbackTaskId;
    const teamsWithoutReviewer = (settings?.teamsWithoutReviewer ?? [])
      .map((warning) => this.mapPeerReviewWarning(warning))
      .filter((warning): warning is PeerReviewWithoutReviewerWarning => warning !== null);

    return {
      taskId,
      peerReviewEnabled: Boolean(settings?.peerReviewEnabled),
      peerReviewDistributionType: this.resolvePeerReviewDistributionType(settings?.peerReviewDistributionType),
      peerReviewerVisibleToTeams: Boolean(settings?.peerReviewerVisibleToTeams),
      peerReviewConfirmedAt: this.asTrimmedString(settings?.peerReviewConfirmedAt) || null,
      hasTeamsWithoutReviewer: Boolean(settings?.hasTeamsWithoutReviewer) || teamsWithoutReviewer.length > 0,
      assignments: (settings?.assignments ?? []).map((assignment) => this.mapPeerReviewAssignment(assignment, taskId)),
      teamsWithoutReviewer,
    };
  }

  private emptyPeerReviewSettings(taskId: string): PeerReviewSettings {
    return {
      taskId,
      peerReviewEnabled: false,
      peerReviewDistributionType: null,
      peerReviewerVisibleToTeams: false,
      peerReviewConfirmedAt: null,
      hasTeamsWithoutReviewer: false,
      assignments: [],
      teamsWithoutReviewer: [],
    };
  }

  private mapPeerReviewAssignment(
    assignment: PeerReviewAssignmentResponse | null | undefined,
    fallbackTaskId: string,
  ): PeerReviewAssignment {
    return {
      id: this.toId(assignment?.id) || `peer-assignment-${Math.random()}`,
      taskId: this.toId(assignment?.taskId) || fallbackTaskId,
      reviewerTeamId: this.toId(assignment?.reviewerTeamId) || null,
      reviewedTeamId: this.toId(assignment?.reviewedTeamId) || null,
      targetParticipationId: this.toId(assignment?.targetParticipationId) || null,
      assessmentId: this.toId(assignment?.assessmentId) || null,
      status: this.asTrimmedString(assignment?.status) || 'ASSIGNED',
      createdAt: this.asTrimmedString(assignment?.createdAt) || null,
      submittedAt: this.asTrimmedString(assignment?.submittedAt) || null,
      teacherEditorId: this.toId(assignment?.teacherEditorId) || null,
      teacherEditedAt: this.asTrimmedString(assignment?.teacherEditedAt) || null,
    };
  }

  private mapPeerReviewWarning(
    warning: PeerReviewWithoutReviewerWarningResponse | null | undefined,
  ): PeerReviewWithoutReviewerWarning | null {
    const teamId = this.toId(warning?.teamId);
    const teamName = this.asTrimmedString(warning?.teamName);
    if (!teamId && !teamName) {
      return null;
    }

    return {
      assignmentId: this.toId(warning?.assignmentId) || null,
      teamId,
      teamName: teamName || 'РљРѕРјР°РЅРґР° Р±РµР· РѕС†РµРЅС‰РёРєР°',
      message: this.asTrimmedString(warning?.message),
    };
  }

  private resolvePeerReviewDistributionType(value: unknown): PeerReviewDistributionType | null {
    const normalized = this.asTrimmedString(value).toUpperCase();
    if (
      normalized === 'MANUAL'
      || normalized === 'PAIR'
      || normalized === 'CIRCLE'
      || normalized === 'RANDOM_PAIR'
      || normalized === 'RANDOM_CIRCLE'
    ) {
      return normalized;
    }
    return null;
  }

  private mapPeerAssessmentCriterionResult(
    item: ParticipationAssessmentItemResponse | null | undefined,
  ): PeerAssessmentCriterionResult {
    return {
      criterionId: this.asTrimmedString(item?.criterionId),
      title: this.asTrimmedString(item?.title) || 'Критерий',
      description: this.asTrimmedString(item?.description),
      maxPoints: this.resolveFiniteNumber(item?.maxPoints) ?? 0,
      sectionName: this.asTrimmedString(item?.sectionName),
      orderIndex: this.resolveFiniteNumber(item?.orderIndex) ?? 0,
      points: this.resolveFiniteNumber(item?.points),
      comment: typeof item?.comment === 'string' ? item.comment.trim() || null : null,
    };
  }

  private mapParticipationAssessmentItem(
    item: ParticipationAssessmentItemResponse | null | undefined,
  ): ParticipationAssessmentItem {
    return {
      criterionId: (item?.criterionId ?? '').trim(),
      title: (item?.title ?? '').trim(),
      description: (item?.description ?? '').trim(),
      maxPoints: typeof item?.maxPoints === 'number' && Number.isFinite(item.maxPoints) ? item.maxPoints : 0,
      sectionName: (item?.sectionName ?? '').trim(),
      orderIndex: typeof item?.orderIndex === 'number' && Number.isFinite(item.orderIndex) ? item.orderIndex : 0,
      active: item?.active ?? true,
      points: typeof item?.points === 'number' && Number.isFinite(item.points) ? item.points : null,
      comment: typeof item?.comment === 'string' ? item.comment : null,
      teacherPoints:
        typeof item?.teacherPoints === 'number' && Number.isFinite(item.teacherPoints) ? item.teacherPoints : null,
      selfPoints: typeof item?.selfPoints === 'number' && Number.isFinite(item.selfPoints) ? item.selfPoints : null,
      teacherComment: typeof item?.teacherComment === 'string' ? item.teacherComment : null,
      selfComment: typeof item?.selfComment === 'string' ? item.selfComment : null,
    };
  }

  private normalizeGroups(groups: TeacherCourseResponse[] | null | undefined): TeacherGroup[] {
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
      peerReviewEnabled: Boolean(task?.peerReviewEnabled),
      peerReviewDistributionType: this.resolvePeerReviewDistributionType(task?.peerReviewDistributionType),
      peerReviewerVisibleToTeams: Boolean(task?.peerReviewerVisibleToTeams),
      peerReviewConfirmedAt: this.asTrimmedString(task?.peerReviewConfirmedAt) || null,
      teams: (task?.teams ?? []).map((t) => this.mapTeam(t)),
    };
  }

  private mapTeam(team: TeacherTeamResponse | null | undefined): TaskTeam {
    const participations = (team?.participations ?? [])
      .map((item) => ({
        id: (item?.id ?? '').trim(),
        studentId: (item?.studentId ?? '').trim(),
        studentName: (item?.studentName ?? '').trim(),
        mark: typeof item?.mark === 'number' && Number.isFinite(item.mark) ? item.mark : null,
        attachments: this.mapAttachments(
          item?.attachments ?? [],
          {
            studentName: (item?.studentName ?? '').trim() || null,
            teamName: (team?.name ?? '').trim() || null,
            participationId: (item?.id ?? '').trim() || null,
          },
        ),
      }))
      .filter((item) => Boolean(item.id) && Boolean(item.studentId));

    return {
      id: (team?.id ?? '').trim(),
      name: (team?.name ?? '').trim() || 'Команда',
      membersCount: typeof team?.membersCount === 'number' ? team.membersCount : participations.length,
      captainId: (team?.captainId ?? null) ? String(team?.captainId) : null,
      participations,
    };
  }

  private normalizeNullableNumber(value: unknown): number | null {
    return typeof value === 'number' && Number.isFinite(value) ? value : null;
  }

  private normalizeTeamTypeForApi(value: string | null | undefined): 'RANDOM' | 'FREEROAM' | 'DRAFT' {
    const normalized = String(value ?? '').trim().toUpperCase();
    if (normalized === 'RANDOM' || normalized === 'DRAFT' || normalized === 'FREEROAM') {
      return normalized;
    }
    return 'FREEROAM';
  }

  private mapTaskAttachments(task: TeacherTaskResponse | null | undefined): TeacherTask['attachedWorks'] {
    const legacyTaskAttachments = this.mapAttachments(task?.attachmentDownloadInfos ?? [], {
      studentName: null,
      teamName: null,
      participationId: null,
    });

    const participationAttachments = (task?.teams ?? []).flatMap((team) =>
      (team.participations ?? []).flatMap((participation) =>
        this.mapAttachments(participation.attachments ?? [], {
          studentName: (participation.studentName ?? '').trim() || null,
          teamName: (team.name ?? '').trim() || null,
          participationId: (participation.id ?? '').trim() || null,
        }),
      ),
    );

    return this.deduplicateAttachments([...participationAttachments, ...legacyTaskAttachments]);
  }

  private mapAttachments(
    attachments: TeacherAttachmentResponse[],
    meta: Pick<TeacherTask['attachedWorks'][number], 'studentName' | 'teamName' | 'participationId'>,
  ): TeacherTask['attachedWorks'] {
    return attachments
      .map((attachment) => this.mapTaskAttachment(attachment, meta))
      .filter((attachment): attachment is TeacherTask['attachedWorks'][number] => attachment !== null);
  }

  private mapTaskAttachment(
    attachment: TeacherAttachmentResponse | null | undefined,
    meta: Pick<TeacherTask['attachedWorks'][number], 'studentName' | 'teamName' | 'participationId'>,
  ): TeacherTask['attachedWorks'][number] | null {
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

    if (!id && !objectKey && !fileName) {
      return null;
    }

    return {
      id,
      fileName,
      fileType,
      fileSize,
      objectKey,
      studentName: meta.studentName,
      teamName: meta.teamName,
      participationId: meta.participationId,
    };
  }

  private deduplicateAttachments(attachments: TeacherTask['attachedWorks']): TeacherTask['attachedWorks'] {
    const byKey = new Map<string, TeacherTask['attachedWorks'][number]>();
    for (const attachment of attachments) {
      const key = String(attachment.id ?? attachment.objectKey ?? `${attachment.participationId}:${attachment.fileName}`).trim();
      if (!key || byKey.has(key)) {
        continue;
      }
      byKey.set(key, attachment);
    }

    return [...byKey.values()];
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
