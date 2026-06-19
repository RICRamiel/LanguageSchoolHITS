import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import {
  StudentParticipationAssessment,
  StudentTask,
  StudentTaskComment,
  StudentTaskPeerAssessment,
} from '../../student-page.types';
import { FormsModule } from '@angular/forms';
import { PeerAssessmentFormComponent } from '../peer-assessment-form/peer-assessment-form.component';
import { PeerAssessmentSubmitItem } from '../../../../core/peer-assessment/peer-assessment.contracts';
import { NotificationAttachment } from '../../../../core/teacher/teacher.models';

@Component({
  selector: 'app-student-task-details-modal',
  standalone: true,
  imports: [FormsModule, PeerAssessmentFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './student-task-details-modal.component.html',
  styleUrl: './student-task-details-modal.component.less',
})
export class StudentTaskDetailsModalComponent {
  readonly task = input.required<StudentTask>();
  readonly uploadInProgress = input<boolean>(false);
  readonly completeInProgress = input<boolean>(false);
  readonly commentsLoading = input<boolean>(false);
  readonly commentSubmitting = input<boolean>(false);
  readonly uploadedFileLink = input<string | null>(null);
  readonly comments = input<StudentTaskComment[]>([]);
  readonly teamActionInProgress = input<boolean>(false);
  readonly teamError = input<string | null>(null);
  readonly assessmentLoading = input<boolean>(false);
  readonly assessmentSaving = input<boolean>(false);
  readonly assessmentError = input<string | null>(null);
  readonly assessment = input<StudentParticipationAssessment | null>(null);
  readonly peerAssessmentLoading = input<boolean>(false);
  readonly peerAssessmentSaving = input<boolean>(false);
  readonly peerAssessmentError = input<string | null>(null);
  readonly peerAssessment = input<StudentTaskPeerAssessment | null>(null);
  readonly close = output<void>();
  readonly uploadFile = output<File>();
  readonly downloadAttachment = output<NotificationAttachment>();
  readonly completeTask = output<void>();
  readonly submitComment = output<string>();
  readonly createTeam = output<string>();
  readonly joinTeam = output<string>();
  readonly saveSelfAssessment = output<Array<{ criterionId: string; points: number; comment: string }>>();
  readonly savePeerAssessment = output<PeerAssessmentSubmitItem[]>();

  readonly selectedFile = signal<File | null>(null);
  readonly commentText = signal('');
  readonly newTeamName = signal('');
  readonly selfScores = signal<Record<string, string>>({});
  readonly selfComments = signal<Record<string, string>>({});

  onFileChange(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    const file = target?.files?.[0] ?? null;
    this.selectedFile.set(file);
  }

  submitFile(): void {
    const file = this.selectedFile();
    if (!file || this.uploadInProgress()) {
      return;
    }
    this.uploadFile.emit(file);
  }

  submitCompleteTask(): void {
    if (this.completeInProgress()) {
      return;
    }
    this.completeTask.emit();
  }

  onCommentInput(event: Event): void {
    const target = event.target as HTMLTextAreaElement | null;
    this.commentText.set((target?.value ?? '').trimStart());
  }

  sendComment(): void {
    const text = this.commentText().trim();
    if (!text || this.commentSubmitting()) {
      return;
    }

    this.submitComment.emit(text);
    this.commentText.set('');
  }

  onTeamNameInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.newTeamName.set(target?.value ?? '');
  }

  onCreateTeam(): void {
    const name = this.newTeamName().trim();
    if (!name || this.teamActionInProgress()) {
      return;
    }
    this.createTeam.emit(name);
    this.newTeamName.set('');
  }

  onJoinTeam(teamId: string): void {
    if (this.teamActionInProgress()) {
      return;
    }
    this.joinTeam.emit(teamId);
  }

  getSelfScore(criterionId: string, fallback: number | null): string {
    const local = this.selfScores()[criterionId];
    if (local !== undefined) {
      return local;
    }
    return fallback === null ? '' : String(fallback);
  }

  getSelfComment(criterionId: string, fallback: string | null): string {
    const local = this.selfComments()[criterionId];
    if (local !== undefined) {
      return local;
    }
    return fallback ?? '';
  }

  onSelfScoreInput(criterionId: string, value: string): void {
    this.selfScores.update((state) => ({ ...state, [criterionId]: value }));
  }

  onSelfCommentInput(criterionId: string, value: string): void {
    this.selfComments.update((state) => ({ ...state, [criterionId]: value }));
  }

  submitSelfAssessment(): void {
    const assessment = this.assessment();
    if (!assessment || this.assessmentSaving()) {
      return;
    }

    const items = assessment.criteria
      .filter((criterion) => criterion.active)
      .map((criterion) => {
        const rawPoints = this.getSelfScore(criterion.criterionId, criterion.selfPoints);
        const parsedPoints = Number(rawPoints);
        return {
          criterionId: criterion.criterionId,
          points: Number.isFinite(parsedPoints) ? parsedPoints : 0,
          comment: this.getSelfComment(criterion.criterionId, criterion.selfComment).trim(),
        };
      });

    this.saveSelfAssessment.emit(items);
  }
}
