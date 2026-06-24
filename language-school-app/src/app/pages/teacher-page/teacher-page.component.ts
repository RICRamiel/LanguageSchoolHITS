import { AsyncPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, combineLatest, finalize, forkJoin, map, of, shareReplay, switchMap, tap } from 'rxjs';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  NotificationAttachment,
  ParticipationAssessment,
  PeerAssessmentResult,
  PeerAssessmentEditItem,
  PeerReviewEnablePayload,
  PeerReviewManualAssignmentPayload,
  PeerReviewSettings,
  AssessmentSubmitItem,
  TaskCriterion,
  TaskCriterionPayload,
  TaskDetailsOpenPayload,
  TeacherGroup,
  TeacherNotification,
  TeacherStudentGrade,
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
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);

  private teacherId: string | null = null;

  private readonly groupsSubject = new BehaviorSubject<TeacherGroup[]>([]);
  private readonly allTasksSubject = new BehaviorSubject<TeacherTask[]>([]);
  private readonly allNotificationsSubject = new BehaviorSubject<TeacherNotification[]>([]);
  private readonly selectedGroupFilterSubject = new BehaviorSubject<string>('all');
  private readonly gradeStudentsSubject = new BehaviorSubject<TeacherStudentGrade[]>([]);

  private tasksSnapshot: TeacherTask[] = [];
  private notificationsSnapshot: TeacherNotification[] = [];

  fullName = '';
  email = '';
  badge = 'Преподаватель';
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
      const groupId = selectedGroupFilter;
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

      const groupId = selectedGroupFilter;
      const selectedGroup = groups.find((group) => group.id === groupId);
      if (!selectedGroup) {
        return of(allTasks);
      }

      return this.teacherService.getTasksByCourseId(selectedGroup.id).pipe(
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
  gradingStudentsLoading = false;
  gradingStudentsError: string | null = null;
  gradingTaskId: string | null = null;
  gradingTaskTitle = '';
  teamCreating = false;
  studentAdding = false;
  courseStudentsSnapshot: { id: string; fullName: string }[] = [];
  selectedTask: TeacherTask | null = null;
  selectedTaskSection: TeacherTaskDetailsSection = 'overview';
  selectedNotification: TeacherNotification | null = null;
  taskCriteria: TaskCriterion[] = [];
  taskCriteriaLoading = false;
  taskCriteriaSaving = false;
  taskCriteriaError: string | null = null;
  taskFinalizing = false;
  taskFinalizeError: string | null = null;
  peerAssessmentResults: PeerAssessmentResult[] = [];
  peerAssessmentResultsLoading = false;
  peerAssessmentResultsError: string | null = null;
  peerAssessmentEditSaving = false;
  peerAssessmentEditError: string | null = null;
  peerReviewSettings: PeerReviewSettings | null = null;
  peerReviewSettingsLoading = false;
  peerReviewSettingsSaving = false;
  peerReviewSettingsError: string | null = null;
  peerReviewManualAssignmentSaving = false;
  peerReviewConfirmSaving = false;
  selectedAssessmentStudent: TeacherStudentGrade | null = null;
  selectedAssessmentParticipationId: string | null = null;
  teacherAssessment: ParticipationAssessment | null = null;
  teacherAssessmentLoading = false;
  teacherAssessmentSaving = false;
  teacherAssessmentError: string | null = null;
  teacherAssessmentDraft: Record<string, { points: string; comment: string }> = {};
  readonly gradingStudents$ = this.gradeStudentsSubject.asObservable();

  ngOnInit(): void {
    this.tasks$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((tasks) => {
      this.tasksSnapshot = tasks;
    });

    this.notifications$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((notifications) => {
      this.notificationsSnapshot = notifications;
    });

    this.gradingStudents$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((students: TeacherStudentGrade[]) => {
      this.courseStudentsSnapshot = students.map((s: TeacherStudentGrade) => ({ id: s.id, fullName: s.fullName }));
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
    this.teacherService.createTask(payload).pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
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
    this.teacherService.createNotification(payload).pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
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
    this.loadTaskCriteria(task.id);
    this.loadPeerReviewSettings(task.id);
    this.loadPeerAssessmentResults(task);

    if (task.id) {
      this.teacherService.getTaskComments(task.id).pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (comments) => {
          this.applyTaskComments(task.id, comments);
        },
      });
    }
  }

  onSubmitTaskComment(text: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || !this.teacherId) {
      return;
    }

    this.teacherService
      .createComment(taskId, this.teacherId, text)
      .pipe(switchMap(() => this.teacherService.getTaskComments(taskId)))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
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
    this.teamCreating = false;
    this.taskCriteria = [];
    this.taskCriteriaLoading = false;
    this.taskCriteriaSaving = false;
    this.taskCriteriaError = null;
    this.taskFinalizing = false;
    this.taskFinalizeError = null;
    this.peerAssessmentResults = [];
    this.peerAssessmentResultsLoading = false;
    this.peerAssessmentResultsError = null;
    this.peerAssessmentEditSaving = false;
    this.peerAssessmentEditError = null;
    this.peerReviewSettings = null;
    this.peerReviewSettingsLoading = false;
    this.peerReviewSettingsSaving = false;
    this.peerReviewSettingsError = null;
    this.peerReviewManualAssignmentSaving = false;
    this.peerReviewConfirmSaving = false;
  }

  onCreateCriterion(payload: TaskCriterionPayload): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.taskCriteriaSaving) {
      return;
    }

    this.taskCriteriaSaving = true;
    this.taskCriteriaError = null;

    this.teacherService.createTaskCriterion(taskId, payload).pipe(
      finalize(() => {
        this.taskCriteriaSaving = false;
      }),
      catchError(() => {
        this.taskCriteriaError = 'Не удалось создать критерий';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (criterion) => {
        if (!criterion) {
          return;
        }
        this.taskCriteria = [...this.taskCriteria, criterion].sort((a, b) => a.orderIndex - b.orderIndex);
      },
    });
  }

  onUpdateCriterion(payload: { criterionId: string; payload: TaskCriterionPayload }): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.taskCriteriaSaving) {
      return;
    }

    this.taskCriteriaSaving = true;
    this.taskCriteriaError = null;

    this.teacherService.updateTaskCriterion(taskId, payload.criterionId, payload.payload).pipe(
      finalize(() => {
        this.taskCriteriaSaving = false;
      }),
      catchError(() => {
        this.taskCriteriaError = 'Не удалось обновить критерий';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (criterion) => {
        if (!criterion) {
          return;
        }
        this.taskCriteria = this.taskCriteria
          .map((item) => (item.id === criterion.id ? criterion : item))
          .sort((a, b) => a.orderIndex - b.orderIndex);
      },
    });
  }

  onDeactivateCriterion(criterionId: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.taskCriteriaSaving) {
      return;
    }

    this.taskCriteriaSaving = true;
    this.taskCriteriaError = null;

    this.teacherService.deactivateTaskCriterion(taskId, criterionId).pipe(
      finalize(() => {
        this.taskCriteriaSaving = false;
      }),
      catchError(() => {
        this.taskCriteriaError = 'Не удалось деактивировать критерий';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        this.taskCriteria = this.taskCriteria.map((item) =>
          item.id === criterionId ? { ...item, active: false } : item,
        );
      },
    });
  }

  onFinalizeTask(): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.taskFinalizing) {
      return;
    }

    this.taskFinalizing = true;
    this.taskFinalizeError = null;

    this.teacherService.finalizeTask(taskId).pipe(
      finalize(() => {
        this.taskFinalizing = false;
      }),
      catchError(() => {
        this.taskFinalizeError = 'Не удалось финализировать задание';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (task) => {
        if (!task) {
          return;
        }
        this.applySelectedTaskUpdate(task);
      },
    });
  }

  onCreateTeam(name: string): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.teamCreating) {
      return;
    }
    this.teamCreating = true;
    this.teacherService.createTeam(taskId, name).pipe(
      finalize(() => { this.teamCreating = false; }),
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (team) => {
        if (!team || !this.selectedTask) return;
        const updatedTask = { ...this.selectedTask, teams: [...this.selectedTask.teams, team] };
        this.selectedTask = updatedTask;
        this.allTasksSubject.next(
          this.allTasksSubject.value.map((t) => t.id === updatedTask.id ? updatedTask : t),
        );
      },
    });
  }

  onAddStudentToTeam(payload: { teamId: string; studentId: string }): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.studentAdding) {
      return;
    }
    this.studentAdding = true;
    this.teacherService.addStudentToTeam(taskId, payload.teamId, payload.studentId).pipe(
      finalize(() => { this.studentAdding = false; }),
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (updatedTeam) => {
        if (!updatedTeam || !this.selectedTask) return;
        const updatedTask = {
          ...this.selectedTask,
          teams: this.selectedTask.teams.map((t) => t.id === updatedTeam.id ? updatedTeam : t),
        };
        this.selectedTask = updatedTask;
        this.allTasksSubject.next(
          this.allTasksSubject.value.map((t) => t.id === updatedTask.id ? updatedTask : t),
        );
      },
    });
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
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
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
    this.authService.logout().pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  onGroupFilterChange(value: string): void {
    this.selectedGroupFilter = value;
    this.selectedGroupFilterSubject.next(value);
    this.loadStudentsForGrading();
  }

  onStudentGradeInput(studentId: string, grade: string): void {
    this.gradeStudentsSubject.next(
      this.gradeStudentsSubject.value.map((student) =>
        student.id === studentId
          ? {
              ...student,
              grade,
              error: null,
            }
          : student,
      ),
    );
  }

  onSaveStudentGrade(studentId: string): void {
    const student = this.gradeStudentsSubject.value.find((item) => item.id === studentId);
    if (!student) {
      return;
    }

    this.gradeStudentsSubject.next(
      this.gradeStudentsSubject.value.map((item) =>
        item.id === studentId
          ? {
              ...item,
              saving: true,
              error: null,
            }
          : item,
      ),
    );

    this.teacherService
      .updateStudentGrade(student)
      .pipe(
        finalize(() => {
          this.gradeStudentsSubject.next(
            this.gradeStudentsSubject.value.map((item) =>
              item.id === studentId
                ? {
                    ...item,
                    saving: false,
                  }
                : item,
            ),
          );
        }),
        catchError(() => {
          this.gradeStudentsSubject.next(
            this.gradeStudentsSubject.value.map((item) =>
              item.id === studentId
                ? {
                    ...item,
                    error: 'Не удалось сохранить оценку',
                  }
                : item,
            ),
          );
          return of(void 0);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  onOpenGradingFromTask(payload: { taskId: string; group: string; title: string }): void {
    this.gradingTaskId = payload.taskId;
    this.gradingTaskTitle = payload.title;

    const normalized = payload.group.trim().toLowerCase();
    const match = this.groupsSubject.value.find((group) => group.name.trim().toLowerCase() === normalized);
    if (match) {
      const value = String(match.id);
      this.selectedGroupFilter = value;
      this.selectedGroupFilterSubject.next(value);
      this.loadStudentsForGrading();
    }

    if (typeof document !== 'undefined') {
      requestAnimationFrame(() => {
        document.getElementById('teacher-grading')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    }
  }

  private loadTeacherDashboard(): void {
    this.userService
      .getMe()
      .pipe(
        tap((profile) => {
          this.teacherId = String(profile.id ?? '').trim() || null;
          this.fullName = [profile.lastName, profile.firstName].filter(Boolean).join(' ').trim();
          this.email = profile.email;
        }),
        switchMap((profile) => {
          const fallbackGroups: TeacherGroup[] = (profile.groups ?? [])
            .map((group) => ({
              id: String(group.id ?? '').trim(),
              name: (group.name ?? '').trim(),
            }))
            .filter((group) => Boolean(group.id) && Boolean(group.name));

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
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: ({ groups, tasks, notifications }) => {
          this.groupsSubject.next(groups);
          this.allTasksSubject.next(tasks);
          this.allNotificationsSubject.next(notifications);
          if (this.selectedGroupFilter === 'all' && groups.length > 0) {
            const firstGroupId = String(groups[0].id);
            this.selectedGroupFilter = firstGroupId;
            this.selectedGroupFilterSubject.next(firstGroupId);
          }
          this.loadStudentsForGrading();
        },
        error: () => {
          this.groupsSubject.next([]);
          this.allTasksSubject.next([]);
          this.allNotificationsSubject.next([]);
          this.gradeStudentsSubject.next([]);
          this.gradingStudentsLoading = false;
          this.gradingStudentsError = null;
          this.selectedGroupFilter = 'all';
          this.selectedGroupFilterSubject.next('all');
        },
      });
  }

  private loadStudentsForGrading(): void {
    const groupId = this.extractSelectedGroupId();
    if (!groupId) {
      this.gradeStudentsSubject.next([]);
      this.gradingStudentsLoading = false;
      this.gradingStudentsError = null;
      return;
    }

    this.gradingStudentsLoading = true;
    this.gradingStudentsError = null;

    this.teacherService
      .getStudentsByGroup(groupId)
      .pipe(
        finalize(() => {
          this.gradingStudentsLoading = false;
        }),
        catchError(() => {
          this.gradingStudentsError = 'Не удалось загрузить студентов курса';
          return of([] as TeacherStudentGrade[]);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (students) => {
          this.gradeStudentsSubject.next(students);
        },
      });
  }

  onOpenTeacherAssessment(studentId: string): void {
    const student = this.gradeStudentsSubject.value.find((item) => item.id === studentId) ?? null;
    const participationId = this.resolveParticipationId(studentId);
    if (!student || !this.gradingTaskId) {
      return;
    }
    if (!participationId) {
      this.gradeStudentsSubject.next(
        this.gradeStudentsSubject.value.map((item) =>
          item.id === studentId
            ? {
                ...item,
                error: 'Студент не участвует в выбранном задании',
              }
            : item,
        ),
      );
      return;
    }

    this.selectedAssessmentStudent = student;
    this.selectedAssessmentParticipationId = participationId;
    this.teacherAssessmentDraft = {};
    this.loadTeacherAssessment();
  }

  canOpenTeacherAssessment(studentId: string): boolean {
    if (!this.gradingTaskId) {
      return false;
    }
    return Boolean(this.resolveParticipationId(studentId));
  }

  onTeacherAssessmentPointsChange(criterionId: string, value: string): void {
    const existing = this.teacherAssessmentDraft[criterionId] ?? { points: '', comment: '' };
    this.teacherAssessmentDraft = {
      ...this.teacherAssessmentDraft,
      [criterionId]: { ...existing, points: value },
    };
  }

  onTeacherAssessmentCommentChange(criterionId: string, value: string): void {
    const existing = this.teacherAssessmentDraft[criterionId] ?? { points: '', comment: '' };
    this.teacherAssessmentDraft = {
      ...this.teacherAssessmentDraft,
      [criterionId]: { ...existing, comment: value },
    };
  }

  getTeacherAssessmentPoints(criterionId: string, fallback: number | null): string {
    const value = this.teacherAssessmentDraft[criterionId]?.points;
    if (value !== undefined) {
      return value;
    }
    return fallback === null ? '' : String(fallback);
  }

  getTeacherAssessmentComment(criterionId: string, fallback: string | null): string {
    const value = this.teacherAssessmentDraft[criterionId]?.comment;
    if (value !== undefined) {
      return value;
    }
    return fallback ?? '';
  }

  closeTeacherAssessmentModal(): void {
    this.selectedAssessmentStudent = null;
    this.selectedAssessmentParticipationId = null;
    this.teacherAssessment = null;
    this.teacherAssessmentLoading = false;
    this.teacherAssessmentSaving = false;
    this.teacherAssessmentError = null;
    this.teacherAssessmentDraft = {};
  }

  onSubmitTeacherAssessment(): void {
    if (!this.gradingTaskId || !this.selectedAssessmentParticipationId || !this.teacherAssessment) {
      return;
    }

    const items: AssessmentSubmitItem[] = this.teacherAssessment.criteria
      .filter((criterion) => criterion.active)
      .map((criterion) => {
        const pointsValue = this.getTeacherAssessmentPoints(criterion.criterionId, criterion.teacherPoints);
        const parsedPoints = Number(pointsValue);
        return {
          criterionId: criterion.criterionId,
          points: Number.isFinite(parsedPoints) ? parsedPoints : 0,
          comment: this.getTeacherAssessmentComment(criterion.criterionId, criterion.teacherComment).trim(),
        };
      });

    this.teacherAssessmentSaving = true;
    this.teacherAssessmentError = null;

    this.teacherService.submitTeacherAssessment(this.gradingTaskId, this.selectedAssessmentParticipationId, items).pipe(
      switchMap(() => this.teacherService.getParticipationAssessment(this.gradingTaskId!, this.selectedAssessmentParticipationId!)),
      catchError(() => {
        this.teacherAssessmentError = 'Не удалось сохранить оценку преподавателя';
        return of(null);
      }),
      finalize(() => {
        this.teacherAssessmentSaving = false;
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (assessment) => {
        if (!assessment) {
          return;
        }
        this.teacherAssessment = assessment;
      },
    });
  }

  private loadTaskCriteria(taskId: string): void {
    this.taskCriteriaLoading = true;
    this.taskCriteriaError = null;
    this.taskCriteria = [];

    this.teacherService.getTaskCriteria(taskId).pipe(
      finalize(() => {
        this.taskCriteriaLoading = false;
      }),
      catchError(() => {
        this.taskCriteriaError = 'Не удалось загрузить критерии';
        return of([] as TaskCriterion[]);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (criteria) => {
        this.taskCriteria = criteria;
      },
    });
  }

  onEditPeerAssessment(payload: { assignmentId: string; items: PeerAssessmentEditItem[] }): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.peerAssessmentEditSaving) {
      return;
    }

    this.peerAssessmentEditSaving = true;
    this.peerAssessmentEditError = null;

    this.teacherService.editPeerAssessment(taskId, payload.assignmentId, payload.items).pipe(
      finalize(() => {
        this.peerAssessmentEditSaving = false;
      }),
      catchError(() => {
        this.peerAssessmentEditError = 'Не удалось сохранить изменения';
        return of(void 0);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        if (this.selectedTask) {
          this.loadPeerAssessmentResults(this.selectedTask);
        }
      },
    });
  }

  onEnablePeerReview(payload: PeerReviewEnablePayload): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.peerReviewSettingsSaving) {
      return;
    }

    this.peerReviewSettingsSaving = true;
    this.peerReviewSettingsError = null;

    this.teacherService.enablePeerReview(taskId, payload).pipe(
      switchMap((updatedTask) => {
        if (updatedTask) {
          this.applySelectedTaskUpdate(updatedTask);
        } else if (this.selectedTask) {
          this.applySelectedTaskUpdate({
            ...this.selectedTask,
            peerReviewEnabled: true,
            peerReviewDistributionType: payload.peerReviewDistributionType,
            peerReviewerVisibleToTeams: payload.peerReviewerVisibleToTeams,
          });
        }
        return this.teacherService.getPeerReviewSettings(taskId);
      }),
      finalize(() => {
        this.peerReviewSettingsSaving = false;
      }),
      catchError(() => {
        this.peerReviewSettingsError = 'Не удалось включить peer-оценивание';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (settings) => {
        if (!settings) {
          return;
        }
        this.peerReviewSettings = settings;
        if (this.selectedTask) {
          this.loadPeerAssessmentResults(this.selectedTask);
        }
      },
    });
  }

  onAssignManualPeerReview(payload: PeerReviewManualAssignmentPayload): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.peerReviewManualAssignmentSaving) {
      return;
    }

    this.peerReviewManualAssignmentSaving = true;
    this.peerReviewSettingsError = null;

    this.teacherService.assignManualPeerReview(taskId, payload).pipe(
      switchMap(() => this.teacherService.getPeerReviewSettings(taskId)),
      finalize(() => {
        this.peerReviewManualAssignmentSaving = false;
      }),
      catchError(() => {
        this.peerReviewSettingsError = 'Не удалось назначить оценщика';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (settings) => {
        if (!settings) {
          return;
        }
        this.peerReviewSettings = settings;
        if (this.selectedTask) {
          this.loadPeerAssessmentResults(this.selectedTask);
        }
      },
    });
  }

  onConfirmPeerReview(): void {
    const taskId = this.selectedTask?.id;
    if (!taskId || this.peerReviewConfirmSaving) {
      return;
    }

    this.peerReviewConfirmSaving = true;
    this.peerReviewSettingsError = null;

    this.teacherService.confirmPeerReview(taskId).pipe(
      switchMap((results) => {
        this.peerAssessmentResults = results;
        return this.teacherService.getPeerReviewSettings(taskId);
      }),
      finalize(() => {
        this.peerReviewConfirmSaving = false;
      }),
      catchError(() => {
        this.peerReviewSettingsError = 'Не удалось подтвердить peer-оценивание';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (settings) => {
        if (!settings) {
          return;
        }
        this.peerReviewSettings = settings;
        if (this.selectedTask) {
          this.applySelectedTaskUpdate({
            ...this.selectedTask,
            peerReviewConfirmedAt: settings.peerReviewConfirmedAt,
          });
        }
      },
    });
  }

  private loadPeerReviewSettings(taskId: string): void {
    this.peerReviewSettings = null;
    this.peerReviewSettingsLoading = true;
    this.peerReviewSettingsError = null;

    this.teacherService.getPeerReviewSettings(taskId).pipe(
      finalize(() => {
        this.peerReviewSettingsLoading = false;
      }),
      catchError(() => {
        this.peerReviewSettingsError = 'Не удалось загрузить настройки peer-оценивания';
        return of(null);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (settings) => {
        this.peerReviewSettings = settings;
        if (settings?.peerReviewEnabled && this.selectedTask && !this.selectedTask.peerReviewEnabled) {
          this.applySelectedTaskUpdate({
            ...this.selectedTask,
            peerReviewEnabled: true,
            peerReviewDistributionType: settings.peerReviewDistributionType,
            peerReviewerVisibleToTeams: settings.peerReviewerVisibleToTeams,
            peerReviewConfirmedAt: settings.peerReviewConfirmedAt,
          });
          this.loadPeerAssessmentResults(this.selectedTask);
        }
      },
    });
  }

  private loadPeerAssessmentResults(task: TeacherTask): void {
    this.peerAssessmentResults = [];
    this.peerAssessmentResultsError = null;

    if (!task.peerReviewEnabled && !this.peerReviewSettings?.peerReviewEnabled) {
      this.peerAssessmentResultsLoading = false;
      return;
    }

    this.peerAssessmentResultsLoading = true;

    this.teacherService.getPeerAssessmentResults(task.id).pipe(
      finalize(() => {
        this.peerAssessmentResultsLoading = false;
      }),
      catchError(() => {
        this.peerAssessmentResultsError = 'Не удалось загрузить результаты peer-оценивания';
        return of([] as PeerAssessmentResult[]);
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (results) => {
        this.peerAssessmentResults = results;
      },
    });
  }

  private applySelectedTaskUpdate(task: TeacherTask): void {
    this.selectedTask = task;
    this.allTasksSubject.next(
      this.allTasksSubject.value.map((item) => (item.id === task.id ? task : item)),
    );
  }

  private resolveParticipationId(studentId: string): string | null {
    if (!this.gradingTaskId) {
      return null;
    }
    const task = this.tasksSnapshot.find((item) => item.id === this.gradingTaskId);
    if (!task) {
      return null;
    }

    for (const team of task.teams) {
      const participation = team.participations.find((item) => item.studentId === studentId);
      if (participation?.id) {
        return participation.id;
      }
    }
    return null;
  }

  private loadTeacherAssessment(): void {
    if (!this.gradingTaskId || !this.selectedAssessmentParticipationId) {
      return;
    }

    this.teacherAssessmentLoading = true;
    this.teacherAssessmentError = null;
    this.teacherAssessment = null;
    this.cdr.detectChanges();

    this.teacherService.getParticipationAssessment(this.gradingTaskId, this.selectedAssessmentParticipationId).pipe(
      catchError(() => {
        this.teacherAssessmentError = 'Не удалось загрузить assessment';
        return of(null);
      }),
      finalize(() => {
        this.teacherAssessmentLoading = false;
        this.cdr.detectChanges();
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (assessment) => {
        this.teacherAssessment = assessment;
        this.cdr.detectChanges();
      },
    });
  }

  private extractSelectedGroupId(): string | null {
    if (this.selectedGroupFilter === 'all') {
      return null;
    }
    return this.selectedGroupFilter.trim() || null;
  }

  private buildTabs(notificationCount: number) {
    return [
      { id: 'tasks', label: 'Задания' },
      { id: 'notifications', label: 'Уведомления', badge: notificationCount },
    ];
  }

  private applyTaskComments(taskId: string, comments: TeacherTaskComment[]): void {
    this.allTasksSubject.next(
      this.allTasksSubject.value.map((item) =>
        item.id === taskId
          ? {
              ...item,
              taskComments: comments,
              comments: `${comments.length} комментариев`,
            }
          : item,
      ),
    );

    if (this.selectedTask?.id === taskId) {
      this.selectedTask = {
        ...this.selectedTask,
        taskComments: comments,
        comments: `${comments.length} комментариев`,
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





