import { Component, OnInit, input, output, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import {
  TaskCriterion,
  TaskCriterionPayload,
  PeerAssessmentEditItem,
  PeerAssessmentResult,
  TeacherTask,
  TeacherTaskDetailsSection,
  TeacherTaskSubmission,
} from '../../teacher-page.types';
import { buildPeerEditDraft, buildPeerEditItems, PeerEditDraft, updateDraftComment, updateDraftPoints } from './peer-edit.utils';

export type CourseStudent = { id: string; fullName: string };

@Component({
  selector: 'app-task-details-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-details-modal.component.html',
  styleUrl: './task-details-modal.component.less',
})
export class TaskDetailsModalComponent implements OnInit {
  readonly task = input.required<TeacherTask>();
  readonly activeSection = input<TeacherTaskDetailsSection>('overview');
  readonly courseStudents = input<CourseStudent[]>([]);
  readonly teamCreating = input<boolean>(false);
  readonly studentAdding = input<boolean>(false);
  readonly close = output<void>();
  readonly submitComment = output<string>();
  readonly downloadAttachment = output<TeacherTaskSubmission>();
  readonly createTeam = output<string>();
  readonly addStudentToTeam = output<{ teamId: string; studentId: string }>();
  readonly criteria = input<TaskCriterion[]>([]);
  readonly criteriaLoading = input<boolean>(false);
  readonly criteriaSaving = input<boolean>(false);
  readonly criteriaError = input<string | null>(null);
  readonly peerAssessmentResults = input<PeerAssessmentResult[]>([]);
  readonly peerAssessmentResultsLoading = input<boolean>(false);
  readonly peerAssessmentResultsError = input<string | null>(null);
  readonly peerAssessmentEditSaving = input<boolean>(false);
  readonly peerAssessmentEditError = input<string | null>(null);
  readonly createCriterion = output<TaskCriterionPayload>();
  readonly editPeerAssessment = output<{ assignmentId: string; items: PeerAssessmentEditItem[] }>();
  readonly updateCriterion = output<{ criterionId: string; payload: TaskCriterionPayload }>();
  readonly deactivateCriterion = output<string>();
  readonly selectedSection = signal<TeacherTaskDetailsSection>('overview');
  readonly commentText = signal('');
  readonly newTeamName = signal('');
  readonly selectedStudentForTeam = signal<Record<string, string>>({});
  readonly criterionTitle = signal('');
  readonly criterionDescription = signal('');
  readonly criterionSection = signal('');
  readonly criterionMaxPoints = signal('0');
  readonly criterionOrderIndex = signal('0');
  readonly editingCriterionId = signal<string | null>(null);
  readonly editingPeerResultId = signal<string | null>(null);
  readonly peerEditDraft = signal<PeerEditDraft>({});
  readonly teamsWithoutReviewer = computed(() =>
    this.peerAssessmentResults()
      .filter((r) => r.status === 'WITHOUT_REVIEWER')
      .map((r) => r.reviewedTeamName),
  );

  ngOnInit(): void {
    this.selectedSection.set(this.activeSection());
  }

  setSection(section: TeacherTaskDetailsSection): void {
    this.selectedSection.set(section);
  }

  onCommentInput(event: Event): void {
    const target = event.target as HTMLTextAreaElement | null;
    this.commentText.set(target?.value ?? '');
  }

  onCommentSubmit(): void {
    const value = this.commentText().trim();
    if (!value) {
      return;
    }
    this.submitComment.emit(value);
    this.commentText.set('');
  }

  onTeamNameInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.newTeamName.set(target?.value ?? '');
  }

  onCreateTeam(): void {
    const name = this.newTeamName().trim();
    if (!name || this.teamCreating()) {
      return;
    }
    this.createTeam.emit(name);
    this.newTeamName.set('');
  }

  getSelectedStudent(teamId: string): string {
    return this.selectedStudentForTeam()[teamId] ?? '';
  }

  onStudentSelect(teamId: string, studentId: string): void {
    this.selectedStudentForTeam.update((map) => ({ ...map, [teamId]: studentId }));
  }

  onAddStudentToTeam(teamId: string): void {
    const studentId = this.selectedStudentForTeam()[teamId];
    if (!studentId || this.studentAdding()) {
      return;
    }
    this.addStudentToTeam.emit({ teamId, studentId });
    this.selectedStudentForTeam.update((map) => ({ ...map, [teamId]: '' }));
  }

  requestAttachmentDownload(work: TeacherTaskSubmission): void {
    if (!work.id) {
      return;
    }
    this.downloadAttachment.emit(work);
  }

  getAssignmentTypeLabel(value: TeacherTask['assignmentType']): string {
    return value === 'INDIVIDUAL' ? 'Индивидуальное' : 'Командное';
  }

  getTeamTypeLabel(value: TeacherTask['teamType']): string {
    switch (value) {
      case 'RANDOM':
        return 'Случайный';
      case 'DRAFT':
        return 'Драфт';
      case 'CUSTOM':
        return 'Кастомный';
      case 'FREEROAM':
      default:
        return 'Свободный';
    }
  }

  getResolveTypeLabel(value: TeacherTask['resolveType']): string {
    switch (value) {
      case 'FIRST_SUBMITTED_SOLUTION':
        return 'Первое решение';
      case 'CAPTAINS_SOLUTION':
        return 'Решение капитана';
      case 'MOST_VOTES_SOLUTION':
        return 'Демократическое большинство';
      case 'AT_LEAST_VOTES_SOLUTION':
        return 'Квалифицированное большинство';
      case 'LAST_SUBMITTED_SOLUTION':
      default:
        return 'Последнее решение';
    }
  }

  canEditPeerResult(result: PeerAssessmentResult): boolean {
    return Boolean(result.assignmentId)
      && result.criteria.length > 0
      && (result.status === 'SUBMITTED' || result.status === 'TEACHER_EDITED');
  }

  getPeerResultStatusLabel(status: string): string {
    switch (status) {
      case 'ASSIGNED':
        return 'Назначено';
      case 'SUBMITTED':
        return 'Отправлено';
      case 'TEACHER_EDITED':
        return 'Изменено преподавателем';
      case 'FINAL':
        return 'Подтверждено';
      case 'WITHOUT_REVIEWER':
        return 'Без оценщика';
      default:
        return status;
    }
  }

  formatRange(min: number | null, max: number | null): string {
    if (min !== null && max !== null) {
      return min === max ? String(min) : `${min} - ${max}`;
    }
    if (min !== null) {
      return `от ${min}`;
    }
    if (max !== null) {
      return `до ${max}`;
    }
    return 'Не задано';
  }

  getTeamsAmountLabel(task: TeacherTask): string {
    const limit = this.formatRange(task.minTeamsAmount, task.maxTeamsAmount);
    if (task.teams.length === 0) {
      return limit;
    }

    return `Создано: ${task.teams.length}; лимит: ${limit}`;
  }

  getTeamMemberCount(team: TeacherTask['teams'][number]): number {
    return team.membersCount ?? team.participations.length;
  }

  onCriterionTitleInput(event: Event): void {
    this.criterionTitle.set((event.target as HTMLInputElement | null)?.value ?? '');
  }

  onCriterionDescriptionInput(event: Event): void {
    this.criterionDescription.set((event.target as HTMLTextAreaElement | null)?.value ?? '');
  }

  onCriterionSectionInput(event: Event): void {
    this.criterionSection.set((event.target as HTMLInputElement | null)?.value ?? '');
  }

  onCriterionMaxPointsInput(event: Event): void {
    this.criterionMaxPoints.set((event.target as HTMLInputElement | null)?.value ?? '0');
  }

  onCriterionOrderIndexInput(event: Event): void {
    this.criterionOrderIndex.set((event.target as HTMLInputElement | null)?.value ?? '0');
  }

  startEditCriterion(criterion: TaskCriterion): void {
    this.editingCriterionId.set(criterion.id);
    this.criterionTitle.set(criterion.title);
    this.criterionDescription.set(criterion.description);
    this.criterionSection.set(criterion.sectionName);
    this.criterionMaxPoints.set(String(criterion.maxPoints));
    this.criterionOrderIndex.set(String(criterion.orderIndex));
  }

  cancelEditCriterion(): void {
    this.editingCriterionId.set(null);
    this.resetCriterionForm();
  }

  onSaveCriterion(): void {
    if (this.criteriaSaving()) {
      return;
    }

    const payload = this.buildCriterionPayload();
    if (!payload) {
      return;
    }

    const editingId = this.editingCriterionId();
    if (editingId) {
      this.updateCriterion.emit({ criterionId: editingId, payload });
    } else {
      this.createCriterion.emit(payload);
    }

    this.cancelEditCriterion();
  }

  onDeactivateCriterion(criterionId: string): void {
    if (!criterionId || this.criteriaSaving()) {
      return;
    }
    this.deactivateCriterion.emit(criterionId);
  }

  isCriterionFormValid(): boolean {
    return this.buildCriterionPayload() !== null;
  }

  startEditPeerResult(result: PeerAssessmentResult): void {
    this.peerEditDraft.set(buildPeerEditDraft(result));
    this.editingPeerResultId.set(result.id);
  }

  cancelEditPeerResult(): void {
    this.editingPeerResultId.set(null);
    this.peerEditDraft.set({});
  }

  onPeerEditPointsInput(criterionId: string, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.peerEditDraft.update((d) => updateDraftPoints(d, criterionId, value));
  }

  onPeerEditCommentInput(criterionId: string, event: Event): void {
    const value = (event.target as HTMLTextAreaElement).value;
    this.peerEditDraft.update((d) => updateDraftComment(d, criterionId, value));
  }

  submitPeerEdit(assignmentId: string): void {
    if (this.peerAssessmentEditSaving()) {
      return;
    }
    const items = buildPeerEditItems(this.peerEditDraft());
    if (!items.length) {
      return;
    }
    this.editPeerAssessment.emit({ assignmentId, items });
  }

  private buildCriterionPayload(): TaskCriterionPayload | null {
    const title = this.criterionTitle().trim();
    const description = this.criterionDescription().trim();
    const sectionName = this.criterionSection().trim();
    const maxPoints = Number(this.criterionMaxPoints());
    const orderIndex = Number(this.criterionOrderIndex());

    if (!title || !Number.isFinite(maxPoints) || maxPoints < 0 || !Number.isFinite(orderIndex) || orderIndex < 0) {
      return null;
    }

    return {
      title,
      description,
      sectionName,
      maxPoints,
      orderIndex,
    };
  }

  private resetCriterionForm(): void {
    this.criterionTitle.set('');
    this.criterionDescription.set('');
    this.criterionSection.set('');
    this.criterionMaxPoints.set('0');
    this.criterionOrderIndex.set('0');
  }
}
