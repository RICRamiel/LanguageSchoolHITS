import { PeerAssessmentCriterionResult, PeerAssessmentEditItem, PeerAssessmentResult } from '../../teacher-page.types';

export type PeerEditDraft = Record<string, { points: string; comment: string }>;

export function buildPeerEditDraft(result: PeerAssessmentResult): PeerEditDraft {
  return buildPeerEditDraftFromCriteria(result.criteria);
}

export function buildPeerEditDraftFromCriteria(criteria: PeerAssessmentCriterionResult[]): PeerEditDraft {
  const draft: PeerEditDraft = {};
  for (const c of criteria) {
    draft[c.criterionId] = {
      points: c.points !== null ? String(c.points) : '',
      comment: c.comment ?? '',
    };
  }
  return draft;
}

export function buildPeerEditItems(draft: PeerEditDraft): PeerAssessmentEditItem[] {
  return Object.entries(draft)
    .filter(([, v]) => v.points !== '' && Number.isFinite(Number(v.points)))
    .map(([criterionId, v]) => ({
      criterionId,
      points: Number(v.points),
      comment: v.comment,
    }));
}

export function updateDraftPoints(
  draft: PeerEditDraft,
  criterionId: string,
  value: string,
): PeerEditDraft {
  return { ...draft, [criterionId]: { ...draft[criterionId], points: value } };
}

export function updateDraftComment(
  draft: PeerEditDraft,
  criterionId: string,
  value: string,
): PeerEditDraft {
  return { ...draft, [criterionId]: { ...draft[criterionId], comment: value } };
}
