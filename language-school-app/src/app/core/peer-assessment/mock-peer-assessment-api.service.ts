import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import {
  PeerAssessmentCriterion,
  PeerAssessmentDraft,
  PeerAssessmentSubmitItem,
  PeerAssessmentTaskContext,
  StudentPeerAssessment,
} from './peer-assessment.contracts';
import {
  createMockPeerAssessment,
  mergePeerAssessmentDraft,
  selectAssignedPeerTeam,
} from './peer-assessment.utils';

@Injectable({ providedIn: 'root' })
export class MockPeerAssessmentApiService {
  private readonly savedDrafts = new Map<string, PeerAssessmentDraft>();

  getAssignedPeerAssessment(
    task: PeerAssessmentTaskContext,
    captainId: string,
  ): Observable<StudentPeerAssessment | null> {
    const targetTeam = selectAssignedPeerTeam(task, captainId);
    if (!targetTeam) {
      return of(null);
    }

    const assessment = createMockPeerAssessment(
      task.id,
      targetTeam,
      MOCK_PEER_CRITERIA,
    );
    const savedDraft = this.savedDrafts.get(this.getDraftKey(task.id, assessment.targetParticipationId));

    return of(savedDraft ? mergePeerAssessmentDraft(assessment, savedDraft) : assessment);
  }

  submitPeerAssessment(
    taskId: string,
    targetParticipationId: string,
    items: PeerAssessmentSubmitItem[],
    currentAssessment: StudentPeerAssessment,
  ): Observable<StudentPeerAssessment> {
    const draft = items.reduce<PeerAssessmentDraft>((accumulator, item) => {
      accumulator[item.criterionId] = {
        points: String(item.points),
        comment: item.comment,
      };
      return accumulator;
    }, {});

    this.savedDrafts.set(this.getDraftKey(taskId, targetParticipationId), draft);
    return of(mergePeerAssessmentDraft(currentAssessment, draft));
  }

  private getDraftKey(taskId: string, targetParticipationId: string): string {
    return `${taskId}:${targetParticipationId}`;
  }
}

const MOCK_PEER_CRITERIA: PeerAssessmentCriterion[] = [
  {
    criterionId: 'peer-content',
    title: 'Content',
    description: 'Completeness and relevance of the submitted work.',
    maxPoints: 5,
    sectionName: 'Peer review',
    orderIndex: 1,
    active: true,
    peerPoints: null,
    peerComment: null,
  },
  {
    criterionId: 'peer-language',
    title: 'Language',
    description: 'Accuracy, clarity, and appropriate vocabulary.',
    maxPoints: 5,
    sectionName: 'Peer review',
    orderIndex: 2,
    active: true,
    peerPoints: null,
    peerComment: null,
  },
  {
    criterionId: 'peer-presentation',
    title: 'Presentation',
    description: 'Structure, formatting, and delivery quality.',
    maxPoints: 5,
    sectionName: 'Peer review',
    orderIndex: 3,
    active: true,
    peerPoints: null,
    peerComment: null,
  },
];
