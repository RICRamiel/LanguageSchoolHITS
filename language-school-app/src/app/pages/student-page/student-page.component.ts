import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, finalize, forkJoin, map, Observable, of, switchMap, timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user/user.service';
import { TeacherService } from '../../core/teacher/teacher.service';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { NotificationListComponent } from '../../shared/ui/notification-list/notification-list.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import { TaskCardComponent } from '../../shared/ui/task-card/task-card.component';
import { StudentTaskDetailsModalComponent } from './components/student-task-details-modal/student-task-details-modal.component';
import { StudentNotificationModalComponent } from './components/student-notification-modal/student-notification-modal.component';
import {
  StudentNotification,
  StudentParticipationAssessment,
  StudentTask,
  StudentTaskComment,
  StudentTaskPeerAssessment,
  StudentTeam,
} from './student-page.types';
import { OPENAPI_PATHS, withOpenApiBase } from '../../core/api/openapi.config';
import { CommentDTO } from '../../api/model/commentDTO';
import { NotificationAttachment } from '../../core/teacher/teacher.models';
import { PeerAssessmentSubmitItem } from '../../core/peer-assessment/peer-assessment.contracts';
import { MockPeerAssessmentApiService } from '../../core/peer-assessment/mock-peer-assessment-api.service';
import { ErrorToastService } from '../../core/errors/error-toast.service';

type StudentNotificationResponse = {
  id?: string;
  text?: string;
  creationDate?: string;
  groupId?: number | string;
  courseId?: number | string;
  createdByTeacherWithId?: number | string;
  attachmentDownloadInfo?: StudentAttachmentResponse | null;
  attachmentDownloadInfos?: StudentAttachmentResponse[] | null;
  attachment?: StudentAttachmentResponse | null;
  attachments?: StudentAttachmentResponse[] | null;
};

type StudentAttachmentResponse = {
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

type StudentTeamResponse = {
  id?: string;
  name?: string;
  membersCount?: number | null;
  captainId?: string | null;
  participations?: StudentParticipationResponse[] | null;
};

type StudentParticipationResponse = {
  id?: string;
  studentId?: string;
  attachments?: StudentAttachmentResponse[] | null;
};

type StudentTaskResponse = {
  id?: number | string;
  participationId?: string | null;
  name?: string;
  description?: string;
  deadline?: string;
  courseId?: number | string;
  courseName?: string;
  taskStatus?: 'COMPLETE' | 'OVERDUE' | 'PENDING';
  teamType?: string | null;
  maxTeamSize?: number | null;
  currentTeamId?: string | null;
  teams?: StudentTeamResponse[] | null;
  teacher?: {
    firstName?: string;
    lastName?: string;
  };
};

type StudentGroupResponse = {
  id?: number | string;
  name?: string;
};

type StudentGroup = {
  id: string;
  name: string;
};

const RU = {
  tasks: '\u0417\u0430\u0434\u0430\u043D\u0438\u044F',
  notifications: '\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u044F',
  task: '\u0417\u0430\u0434\u0430\u043D\u0438\u0435',
  notification: '\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u0435',
  teacher: '\u041F\u0440\u0435\u043F\u043E\u0434\u0430\u0432\u0430\u0442\u0435\u043B\u044C',
  group: '\u041A\u0443\u0440\u0441',
  allGroups: '\u0412\u0441\u0435 \u043A\u0443\u0440\u0441\u044B',
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
    StudentNotificationModalComponent,
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
  private readonly teacherService = inject(TeacherService);
  private readonly peerAssessmentApi = inject(MockPeerAssessmentApiService);
  private readonly errorToastService = inject(ErrorToastService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);

  studentId = '';
  fullName = '';
  email = '';
  groupName = '';
  groups: StudentGroup[] = [];
  selectedGroupFilter = 'all';

  tabs = [
    { id: 'tasks', label: RU.tasks },
    { id: 'notifications', label: RU.notifications, badge: 0 },
  ];

  activeTab = 'tasks';
  isTaskDetailsModalOpen = false;
  isNotificationDetailsModalOpen = false;
  selectedTask: StudentTask | null = null;
  selectedNotification: StudentNotification | null = null;

  tasks: StudentTask[] = [];
  notifications: StudentNotification[] = [];
  allTasks: StudentTask[] = [];
  allNotifications: StudentNotification[] = [];

  uploadedFileLink: string | null = null;
  uploadInProgress = false;
  completeInProgress = false;
  commentsLoading = false;
  commentSubmitting = false;
  teamActionInProgress = false;
  teamError: string | null = null;
  taskComments: StudentTaskComment[] = [];
  taskAssessment: StudentParticipationAssessment | null = null;
  taskAssessmentLoading = false;
  taskAssessmentSaving = false;
  taskAssessmentError: string | null = null;
  taskPeerAssessment: StudentTaskPeerAssessment | null = null;
  taskPeerAssessmentLoading = false;
  taskPeerAssessmentSaving = false;
  taskPeerAssessmentError: string | null = null;

  ngOnInit(): void {
    this.destroyRef.onDestroy(() => this.clearUploadedFileLink());
    this.loadCurrentUser();
  }

  onTabChange(tabId: string) {
    this.activeTab = tabId;
  }

  onGroupFilterChange(value: string): void {
    this.selectedGroupFilter = value;
    this.applyGroupFilter();
    this.cdr.detectChanges();
  }

  openTaskDetails(taskId: string) {
    const task = this.tasks.find((item) => item.id === taskId);
    if (!task) {
      return;
    }

    this.selectedTask = task;
    this.isTaskDetailsModalOpen = true;
    this.clearUploadedFileLink();
    this.loadTaskComments(taskId);
    this.loadTaskAssessment(task);
    this.loadTaskPeerAssessment(task);
  }

  closeTaskDetailsModal() {
    this.selectedTask = null;
    this.isTaskDetailsModalOpen = false;
    this.clearUploadedFileLink();
    this.uploadInProgress = false;
    this.completeInProgress = false;
    this.commentsLoading = false;
    this.commentSubmitting = false;
    this.teamActionInProgress = false;
    this.teamError = null;
    this.taskComments = [];
    this.taskAssessment = null;
    this.taskAssessmentLoading = false;
    this.taskAssessmentSaving = false;
    this.taskAssessmentError = null;
    this.taskPeerAssessment = null;
    this.taskPeerAssessmentLoading = false;
    this.taskPeerAssessmentSaving = false;
    this.taskPeerAssessmentError = null;
  }

  onSaveSelfAssessment(items: Array<{ criterionId: string; points: number; comment: string }>): void {
    const task = this.selectedTask;
    if (!task?.participationId || this.taskAssessmentSaving) {
      return;
    }

    this.taskAssessmentSaving = true;
    this.taskAssessmentError = null;
    this.cdr.detectChanges();

    this.teacherService.submitSelfAssessment(task.id, task.participationId, items).pipe(
      switchMap(() => this.teacherService.getParticipationAssessment(task.id, task.participationId!)),
      catchError(() => {
        this.taskAssessmentError = 'Не удалось сохранить самооценку';
        return of(null);
      }),
      finalize(() => {
        this.taskAssessmentSaving = false;
        this.cdr.detectChanges();
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (assessment) => {
        if (!assessment) {
          return;
        }
        this.taskAssessment = assessment;
      },
    });
  }

  onSavePeerAssessment(items: PeerAssessmentSubmitItem[]): void {
    const task = this.selectedTask;
    const assessment = this.taskPeerAssessment;
    if (!task || !assessment || this.taskPeerAssessmentSaving) {
      return;
    }

    this.taskPeerAssessmentSaving = true;
    this.taskPeerAssessmentError = null;
    this.cdr.detectChanges();

    this.peerAssessmentApi
      .submitPeerAssessment(task.id, assessment.targetParticipationId, items, assessment)
      .pipe(
        catchError(() => {
          this.taskPeerAssessmentError = 'Не удалось сохранить peer-оценку';
          return of(null);
        }),
        finalize(() => {
          this.taskPeerAssessmentSaving = false;
          this.cdr.detectChanges();
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (updatedAssessment) => {
          if (!updatedAssessment) {
            return;
          }
          this.taskPeerAssessment = updatedAssessment;
        },
      });
  }

  openNotificationDetails(notificationId: string): void {
    const notification = this.notifications.find((item) => item.id === notificationId);
    if (!notification) {
      return;
    }

    this.selectedNotification = notification;
    this.isNotificationDetailsModalOpen = true;
  }

  closeNotificationDetailsModal(): void {
    this.selectedNotification = null;
    this.isNotificationDetailsModalOpen = false;
  }

  onUploadTaskFile(file: File): void {
    const task = this.selectedTask;
    if (!task || this.uploadInProgress) {
      return;
    }

    this.uploadInProgress = true;
    this.cdr.detectChanges();

    const formData = new FormData();
    formData.append('file', file);

    this.ensureParticipationForUpload(task)
      .pipe(
        switchMap((participationId) => {
          if (!participationId) {
            return of(null);
          }
          return this.http
            .post<StudentAttachmentResponse>(
              withOpenApiBase(OPENAPI_PATHS.attachments.uploadToParticipation),
              formData,
              { params: { participationId } },
            )
            .pipe(timeout(15000));
        }),
        catchError(() => of(null)),
        finalize(() => {
          this.uploadInProgress = false;
          this.cdr.detectChanges();
        }),
      )
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (attachment) => {
          const mappedAttachment = this.mapAttachment(attachment);
          if (mappedAttachment) {
            this.appendTaskAttachment(task.id, mappedAttachment);
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

    this.http
      .post<unknown>(withOpenApiBase(`/task/${encodeURIComponent(taskId)}/complete_task`), {})
      .pipe(
        timeout(15000),
        catchError(() => of(null)),
        finalize(() => {
          this.completeInProgress = false;
          this.cdr.detectChanges();
        }),
      )
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (result) => {
          if (result === null) {
            return;
          }
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

  onCreateTeam(name: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.teamActionInProgress) {
      return;
    }
    this.teamActionInProgress = true;
    this.cdr.detectChanges();
    this.http
      .post<StudentTeamResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.teams(taskId)), {
        name,
        captainId: this.studentId || null,
      })
      .pipe(
        catchError(() => of(null)),
        finalize(() => {
          this.teamActionInProgress = false;
          this.cdr.detectChanges();
        }),
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (team) => {
          if (!team || !this.selectedTask) return;
          const newTeam = this.mapTeamResponse(team, name, this.studentId || null);
          const updatedTask = this.upsertTeamInTask(
            this.selectedTask,
            newTeam,
            this.resolveParticipationIdFromTeam(team) ?? this.selectedTask.participationId,
          );
          this.replaceTask(updatedTask);
          this.cdr.detectChanges();
        },
      });
  }

  onJoinTeam(teamId: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.teamActionInProgress) {
      return;
    }
    this.teamError = null;
    this.teamActionInProgress = true;
    this.cdr.detectChanges();
    this.http
      .post<StudentTeamResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.joinTeam(taskId, teamId)), {})
      .pipe(
        catchError((err: unknown) => {
          const body = (err as { error?: Record<string, unknown> })?.error;
          const detail = typeof body?.['detail'] === 'string' ? body['detail'] : null;
          this.teamError = detail ?? 'Не удалось вступить в команду';
          this.cdr.detectChanges();
          return of(null);
        }),
        finalize(() => {
          this.teamActionInProgress = false;
          this.cdr.detectChanges();
        }),
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (team) => {
          if (!team || !this.selectedTask) return;
          this.teamError = null;
          const updatedTask = this.upsertTeamInTask(
            this.selectedTask,
            this.mapTeamResponse(team, 'Команда'),
            this.resolveParticipationIdFromTeam(team) ?? this.selectedTask.participationId,
          );
          this.replaceTask({ ...updatedTask, currentTeamId: team.id ? String(team.id) : teamId });
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
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (comments) => {
          this.taskComments = comments;
          this.cdr.detectChanges();
        },
      });
  }

  onLogout() {
    this.authService.logout().pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  onDownloadNotificationAttachment(attachment: NotificationAttachment): void {
    const attachmentRef = this.resolveAttachmentRef(attachment);
    if (!attachmentRef) {
      return;
    }

    this.http
      .get(withOpenApiBase(OPENAPI_PATHS.attachments.download(attachmentRef)), {
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
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (result) => {
          if (!result?.blob) {
            return;
          }

          this.downloadBlob(result.blob, result.fileName || attachment.fileName || 'вложение');
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
          this.studentId = String(profile.id ?? '');
          this.groupName = profile.groups[0]?.name ?? '';

          const fallbackGroups: StudentGroup[] = (profile.groups ?? [])
            .map((group) => ({
              id: String(group.id ?? '').trim(),
              name: (group.name ?? '').trim(),
            }))
            .filter((group) => Boolean(group.id) && Boolean(group.name));

          const groups$ = this.http
            .get<StudentGroupResponse[]>(
              withOpenApiBase(OPENAPI_PATHS.courses.list),
            )
            .pipe(
              map((groups) =>
                (groups ?? [])
                  .map((group) => ({
                    id: String(group.id ?? '').trim(),
                    name: (group.name ?? '').trim(),
                  }))
                  .filter((group) => Boolean(group.id) && Boolean(group.name)),
              ),
              map((groups) =>
                groups.length ? groups.sort((a, b) => a.name.localeCompare(b.name, 'ru')) : fallbackGroups,
              ),
              catchError(() => of(fallbackGroups)),
            );

          const notifications$ = this.http
            .get<StudentNotificationResponse[]>(
              withOpenApiBase(OPENAPI_PATHS.notifications.forStudent(profile.id)),
            )
            .pipe(catchError(() => of([] as StudentNotificationResponse[])));

          return groups$.pipe(
            switchMap((groups) =>
              this.loadTasksForGroups(groups).pipe(
                map((tasks) => ({
                  groups,
                  tasks,
                })),
              ),
            ),
            switchMap(({ groups, tasks }) =>
              notifications$.pipe(
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
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: ({ groups, tasks, notifications }) => {
          this.groups = groups;
          if (
            this.selectedGroupFilter !== 'all' &&
            !groups.some((group: StudentGroup) => String(group.id) === this.selectedGroupFilter)
          ) {
            this.selectedGroupFilter = 'all';
          }
          this.allTasks = tasks;
          this.allNotifications = notifications.map((item, index) =>
            this.mapNotification(item, index),
          );
          this.applyGroupFilter();
          this.cdr.detectChanges();
        },
        error: () => {
          this.fullName = '';
          this.email = '';
          this.studentId = '';
          this.groupName = '';
          this.groups = [];
          this.allTasks = [];
          this.allNotifications = [];
          this.selectedGroupFilter = 'all';
          this.tasks = [];
          this.notifications = [];
          this.tabs = [
            { id: 'tasks', label: RU.tasks },
            { id: 'notifications', label: RU.notifications, badge: 0 },
          ];
          this.cdr.detectChanges();
        },
      });
  }

  private mapTask(
    task: StudentTaskResponse | null | undefined,
    group: StudentGroup | null,
  ): StudentTask {
    const title = (task?.name ?? '').trim() || RU.task;
    const description = (task?.description ?? '').trim();
    const status = task?.taskStatus ?? 'PENDING';
    const teacherName = [task?.teacher?.lastName, task?.teacher?.firstName]
      .filter(Boolean)
      .join(' ')
      .trim();
    const participationId = this.resolveTaskParticipationId(task);

    return {
      id: String(task?.id ?? Date.now()),
      participationId,
      title,
      pillText: this.mapTaskStatusLabel(status),
      pillVariant: status === 'COMPLETE' ? 'success' : 'neutral',
      teacher: teacherName || RU.teacher,
      description,
      dueText: this.formatDate(task?.deadline),
      groupId: group?.id ?? (task?.courseId != null ? String(task.courseId) : null),
      teamType: task?.teamType ?? null,
      maxTeamSize: typeof task?.maxTeamSize === 'number' ? task.maxTeamSize : null,
      currentTeamId: task?.currentTeamId ?? null,
      teams: (task?.teams ?? []).map((t) => this.mapTeamResponse(t, 'Команда')),
      attachedWorks: this.resolveTaskAttachments(task, participationId),
    };
  }

  private mapTeamResponse(
    team: StudentTeamResponse | null | undefined,
    fallbackName: string,
    fallbackCaptainId: string | null = null,
  ): StudentTeam {
    const participations = team?.participations ?? [];

    return {
      id: String(team?.id ?? ''),
      name: (team?.name ?? fallbackName).trim() || 'Команда',
      membersCount: typeof team?.membersCount === 'number' ? team.membersCount : participations.length || null,
      captainId: team?.captainId ? String(team.captainId) : fallbackCaptainId,
    };
  }

  private resolveTaskParticipationId(task: StudentTaskResponse | null | undefined): string | null {
    const direct = typeof task?.participationId === 'string' ? task.participationId.trim() : '';
    if (direct) {
      return direct;
    }

    return this.findCurrentParticipation(task)?.id?.trim() || null;
  }

  private resolveParticipationIdFromTeam(team: StudentTeamResponse | null | undefined): string | null {
    const participation = (team?.participations ?? []).find((item) =>
      item.studentId != null && String(item.studentId) === this.studentId,
    );
    return participation?.id ? String(participation.id).trim() || null : null;
  }

  private resolveTaskAttachments(
    task: StudentTaskResponse | null | undefined,
    participationId: string | null,
  ): NotificationAttachment[] {
    const participation = this.findCurrentParticipation(task, participationId);
    return this.mapAttachments(participation?.attachments ?? []);
  }

  private findCurrentParticipation(
    task: StudentTaskResponse | null | undefined,
    participationId: string | null = null,
  ): StudentParticipationResponse | null {
    const participations = (task?.teams ?? []).flatMap((team) => team.participations ?? []);
    const normalizedParticipationId = participationId?.trim();

    if (normalizedParticipationId) {
      const byId = participations.find((participation) => String(participation.id ?? '') === normalizedParticipationId);
      if (byId) {
        return byId;
      }
    }

    return participations.find((participation) =>
      participation.studentId != null && String(participation.studentId) === this.studentId,
    ) ?? null;
  }

  private ensureParticipationForUpload(task: StudentTask): Observable<string | null> {
    if (task.participationId) {
      return of(task.participationId);
    }

    if (task.maxTeamSize === 1) {
      return this.http
        .post<StudentTeamResponse>(withOpenApiBase(OPENAPI_PATHS.tasks.teams(task.id)), {
          name: '',
          captainId: this.studentId || null,
        })
        .pipe(
          map((team) => {
            const participationId = this.resolveParticipationIdFromTeam(team);
            if (!participationId) {
              return null;
            }

            this.replaceTask(this.upsertTeamInTask(
              task,
              this.mapTeamResponse(team, 'Команда', this.studentId || null),
              participationId,
            ));
            return participationId;
          }),
          catchError(() => of(null)),
        );
    }

    this.teamError = 'Сначала вступите в команду или создайте ее';
    this.errorToastService.show(this.teamError, 'Нельзя прикрепить файл');
    this.cdr.detectChanges();
    return of(null);
  }

  private upsertTeamInTask(
    task: StudentTask,
    team: StudentTeam,
    participationId: string | null,
  ): StudentTask {
    const teamExists = task.teams.some((item) => item.id === team.id);
    const teams = teamExists
      ? task.teams.map((item) => item.id === team.id ? team : item)
      : [...task.teams, team];

    return {
      ...task,
      participationId,
      currentTeamId: team.id || task.currentTeamId,
      teams,
    };
  }

  private replaceTask(updatedTask: StudentTask): void {
    this.selectedTask = this.selectedTask?.id === updatedTask.id ? updatedTask : this.selectedTask;
    this.allTasks = this.allTasks.map((task) => task.id === updatedTask.id ? updatedTask : task);
    this.tasks = this.tasks.map((task) => task.id === updatedTask.id ? updatedTask : task);
  }

  private appendTaskAttachment(taskId: string, attachment: NotificationAttachment): void {
    const append = (task: StudentTask): StudentTask => {
      if (task.id !== taskId) {
        return task;
      }

      return {
        ...task,
        attachedWorks: this.mergeAttachments(task.attachedWorks, [attachment]),
      };
    };

    this.selectedTask = this.selectedTask?.id === taskId ? append(this.selectedTask) : this.selectedTask;
    this.allTasks = this.allTasks.map(append);
    this.tasks = this.tasks.map(append);
    this.cdr.detectChanges();
  }

  private mergeAttachments(
    current: NotificationAttachment[],
    incoming: NotificationAttachment[],
  ): NotificationAttachment[] {
    const result = [...current];
    for (const attachment of incoming) {
      const key = this.getAttachmentKey(attachment);
      const exists = result.some((item) => this.getAttachmentKey(item) === key);
      if (!exists) {
        result.push(attachment);
      }
    }
    return result;
  }

  private getAttachmentKey(attachment: NotificationAttachment): string {
    return String(attachment.id ?? attachment.objectKey ?? attachment.fileName).trim();
  }

  private loadTaskComments(taskId: string): void {
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
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (comments) => {
          this.taskComments = this.mapTaskComments(comments);
        },
      });
  }

  private mapTaskComments(comments: unknown): StudentTaskComment[] {
    return this.normalizeComments(comments).map((comment) => ({
      userId: comment.userId != null ? String(comment.userId) : null,
      text: this.normalizeText(comment.text),
    }));
  }

  private mapTaskComment(comment: CommentDTO | null | undefined): StudentTaskComment {
    return {
      userId: comment?.userId != null ? String(comment.userId) : null,
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
      author: notification?.createdByTeacherWithId
        ? `${RU.teacher} #${notification.createdByTeacherWithId}`
        : RU.teacher,
      dateTime: this.formatDate(notification?.creationDate),
      text,
      tag: RU.group,
      attachments: this.mapNotificationAttachments(notification),
      groupId: String(notification?.courseId ?? notification?.groupId ?? ''),
    };
  }

  private mapNotificationAttachments(
    notification: StudentNotificationResponse | null | undefined,
  ): NotificationAttachment[] {
    return this.mapAttachments([
      notification?.attachmentDownloadInfo,
      notification?.attachment,
      ...(notification?.attachmentDownloadInfos ?? []),
      ...(notification?.attachments ?? []),
    ]);
  }

  private mapAttachments(
    candidates: Array<StudentAttachmentResponse | null | undefined>,
  ): NotificationAttachment[] {
    return candidates
      .map((candidate) => this.mapAttachment(candidate))
      .filter((attachment): attachment is NotificationAttachment => Boolean(attachment));
  }

  private mapAttachment(
    candidate: StudentAttachmentResponse | null | undefined,
  ): NotificationAttachment | null {
    if (!candidate) {
      return null;
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

    if (!id && !fileName) {
      return null;
    }

    return {
      id,
      objectKey,
      fileName,
      fileType,
      fileSize,
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

  private resolveAttachmentRef(attachment: NotificationAttachment): number | string | null {
    if (typeof attachment.id === 'number' && Number.isFinite(attachment.id) && attachment.id > 0) {
      return String(attachment.id);
    }
    if (typeof attachment.id === 'string') {
      const normalizedId = attachment.id.trim();
      if (normalizedId) {
        return normalizedId;
      }
    }

    const objectKey = attachment.objectKey?.trim();
    return objectKey || null;
  }

  private extractFileName(contentDisposition: string | null): string {
    if (!contentDisposition) {
      return '';
    }

    const utf8Name = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    if (utf8Name) {
      return decodeURIComponent(utf8Name).trim();
    }

    const plainName = contentDisposition.match(/filename=\"?([^\";]+)\"?/i)?.[1];
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

  private clearUploadedFileLink(): void {
    if (this.uploadedFileLink?.startsWith('blob:')) {
      URL.revokeObjectURL(this.uploadedFileLink);
    }
    this.uploadedFileLink = null;
  }

  private loadTasksForGroups(groups: StudentGroup[]): Observable<StudentTask[]> {
    if (!groups.length) {
      return of([]);
    }

    return forkJoin(
      groups.map((group) =>
        this.http
          .get<StudentTaskResponse[]>(
            withOpenApiBase(OPENAPI_PATHS.tasks.byCourse(group.id)),
          )
          .pipe(
            map((tasks) => (tasks ?? []).map((task) => this.mapTask(task, group))),
            catchError(() => of([] as StudentTask[])),
          ),
      ),
    ).pipe(
      map((chunks) => chunks.flat()),
      map((tasks) => {
      const deduplicated = new Map<string, StudentTask>();
        tasks.forEach((task) => deduplicated.set(task.id, task));
        return [...deduplicated.values()];
      }),
    );
  }

  private applyGroupFilter(): void {
    if (this.selectedGroupFilter === 'all') {
      this.tasks = [...this.allTasks];
      this.notifications = [...this.allNotifications];
    } else {
      const groupId = this.selectedGroupFilter;
      this.tasks = this.allTasks.filter((task) => task.groupId === groupId);
      this.notifications = this.allNotifications.filter((item) => item.groupId === groupId);
    }

    this.tabs = [
      { id: 'tasks', label: RU.tasks },
      { id: 'notifications', label: RU.notifications, badge: this.notifications.length },
    ];
  }

  private loadTaskAssessment(task: StudentTask): void {
    if (!task.participationId) {
      this.taskAssessment = null;
      this.taskAssessmentLoading = false;
      this.taskAssessmentError = null;
      return;
    }

    this.taskAssessmentLoading = true;
    this.taskAssessmentError = null;
    this.taskAssessment = null;
    this.cdr.detectChanges();

    this.teacherService.getParticipationAssessment(task.id, task.participationId).pipe(
      catchError(() => {
        this.taskAssessmentError = 'Не удалось загрузить оценку по критериям';
        this.taskAssessmentLoading = false;
        this.taskAssessment = null;
        this.cdr.detectChanges();
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (assessment) => {
        this.taskAssessment = assessment;
        this.taskAssessmentLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadTaskPeerAssessment(task: StudentTask): void {
    this.taskPeerAssessmentLoading = true;
    this.taskPeerAssessmentError = null;
    this.taskPeerAssessment = null;
    this.cdr.detectChanges();

    this.peerAssessmentApi
      .getAssignedPeerAssessment(task, this.studentId)
      .pipe(
        catchError(() => {
          this.taskPeerAssessmentError = 'Не удалось загрузить peer-оценивание';
          return of(null);
        }),
        finalize(() => {
          this.taskPeerAssessmentLoading = false;
          this.cdr.detectChanges();
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (assessment) => {
          this.taskPeerAssessment = assessment;
        },
      });
  }
}

