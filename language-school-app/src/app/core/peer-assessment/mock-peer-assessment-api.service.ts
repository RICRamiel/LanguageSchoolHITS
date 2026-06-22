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
  private readonly submittedKeys = new Set<string>();

  getAssignedPeerAssessment(
    task: PeerAssessmentTaskContext,
    captainId: string,
  ): Observable<StudentPeerAssessment | null> {
    const targetTeam = selectAssignedPeerTeam(task, captainId);
    if (!targetTeam) {
      return of(null);
    }

    const base = createMockPeerAssessment(task.id, targetTeam, MOCK_PEER_CRITERIA);
    const key = this.getDraftKey(task.id, base.targetParticipationId);
    const savedDraft = this.savedDrafts.get(key);
    const submitted = this.submittedKeys.has(key);

    if (savedDraft) {
      const merged = mergePeerAssessmentDraft(base, savedDraft);
      return of({ ...merged, submitted });
    }

    return of({ ...base, submitted });
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

    const key = this.getDraftKey(taskId, targetParticipationId);
    this.savedDrafts.set(key, draft);
    this.submittedKeys.add(key);

    return of({ ...mergePeerAssessmentDraft(currentAssessment, draft), submitted: true });
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
