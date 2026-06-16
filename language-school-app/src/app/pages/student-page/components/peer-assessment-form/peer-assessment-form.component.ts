import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import {
  PeerAssessmentDraft,
  PeerAssessmentSubmitItem,
  StudentPeerAssessment,
} from '../../../../core/peer-assessment/peer-assessment.contracts';
import {
  buildPeerAssessmentItems,
  getPeerAssessmentValidationMessage,
} from '../../../../core/peer-assessment/peer-assessment.utils';

@Component({
  selector: 'app-peer-assessment-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './peer-assessment-form.component.html',
  styleUrl: './peer-assessment-form.component.less',
})
export class PeerAssessmentFormComponent {
  readonly assessment = input<StudentPeerAssessment | null>(null);
  readonly loading = input<boolean>(false);
  readonly saving = input<boolean>(false);
  readonly error = input<string | null>(null);
  readonly savePeerAssessment = output<PeerAssessmentSubmitItem[]>();

  readonly draft = signal<PeerAssessmentDraft>({});
  readonly localValidationError = signal<string | null>(null);
  readonly activeCriteria = computed(() =>
    (this.assessment()?.criteria ?? []).filter((criterion) => criterion.active),
  );

  getPeerScore(criterionId: string, fallback: number | null): string {
    const local = this.draft()[criterionId]?.points;
    if (local !== undefined) {
      return local;
    }
    return fallback === null ? '' : String(fallback);
  }

  getPeerComment(criterionId: string, fallback: string | null): string {
    const local = this.draft()[criterionId]?.comment;
    if (local !== undefined) {
      return local;
    }
    return fallback ?? '';
  }

  onPeerScoreInput(criterionId: string, value: string): void {
    this.localValidationError.set(null);
    this.draft.update((state) => ({
      ...state,
      [criterionId]: {
        points: value,
        comment: state[criterionId]?.comment ?? '',
      },
    }));
  }

  onPeerCommentInput(criterionId: string, value: string): void {
    this.draft.update((state) => ({
      ...state,
      [criterionId]: {
        points: state[criterionId]?.points ?? '',
        comment: value,
      },
    }));
  }

  submitPeerAssessment(): void {
    const assessment = this.assessment();
    if (!assessment || this.saving()) {
      return;
    }

    const result = buildPeerAssessmentItems(assessment.criteria, this.draft());
    if (!result.ok) {
      this.localValidationError.set(getPeerAssessmentValidationMessage(result.error));
      return;
    }

    this.localValidationError.set(null);
    this.savePeerAssessment.emit(result.items);
  }
}
