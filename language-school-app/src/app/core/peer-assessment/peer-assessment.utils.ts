import {
  PeerAssessmentBuildResult,
  PeerAssessmentCriterion,
  PeerAssessmentDraft,
  PeerAssessmentSubmitItem,
  PeerAssessmentTaskContext,
  PeerAssessmentTeamContext,
  StudentPeerAssessment,
} from './peer-assessment.contracts';

export function isCaptainOfCurrentTeam(
  task: PeerAssessmentTaskContext,
  studentId: string,
): boolean {
  const normalizedStudentId = studentId.trim();
  if (!normalizedStudentId || !task.currentTeamId) {
    return false;
  }

  const currentTeam = task.teams.find((team) => team.id === task.currentTeamId);
  return currentTeam?.captainId === normalizedStudentId;
}

export function selectAssignedPeerTeam(
  task: PeerAssessmentTaskContext,
  studentId: string,
): PeerAssessmentTeamContext | null {
  if (!isCaptainOfCurrentTeam(task, studentId)) {
    return null;
  }

  return task.teams.find((team) => team.id && team.id !== task.currentTeamId) ?? null;
}

export function createMockPeerAssessment(
  taskId: string,
  targetTeam: PeerAssessmentTeamContext,
  criteria: PeerAssessmentCriterion[],
  submitted = false,
): StudentPeerAssessment {
  const activeCriteria = sortPeerCriteria(criteria).filter((criterion) => criterion.active);
  const peerTotal = activeCriteria.every((criterion) => criterion.peerPoints !== null)
    ? activeCriteria.reduce((total, criterion) => total + (criterion.peerPoints ?? 0), 0)
    : null;

  return {
    taskId,
    targetTeamId: targetTeam.id,
    targetTeamName: targetTeam.name,
    targetParticipationId: `team:${targetTeam.id}`,
    totalMaxPoints: activeCriteria.reduce((total, criterion) => total + criterion.maxPoints, 0),
    peerTotal,
    submitted,
    criteria: sortPeerCriteria(criteria),
  };
}

export function mergePeerAssessmentDraft(
  assessment: StudentPeerAssessment,
  draft: PeerAssessmentDraft,
): StudentPeerAssessment {
  const criteria = assessment.criteria.map((criterion) => {
    const saved = draft[criterion.criterionId];
    if (!saved) {
      return criterion;
    }

    const parsedPoints = Number(saved.points);
    return {
      ...criterion,
      peerPoints: Number.isFinite(parsedPoints) ? parsedPoints : null,
      peerComment: saved.comment,
    };
  });

  return createMockPeerAssessment(
    assessment.taskId,
    {
      id: assessment.targetTeamId,
      name: assessment.targetTeamName,
      captainId: null,
    },
    criteria,
    assessment.submitted,
  );
}

export function buildPeerAssessmentItems(
  criteria: PeerAssessmentCriterion[],
  draft: PeerAssessmentDraft,
): PeerAssessmentBuildResult {
  const activeCriteria = sortPeerCriteria(criteria).filter((criterion) => criterion.active);
  if (!activeCriteria.length) {
    return { ok: false, error: 'NO_ACTIVE_CRITERIA' };
  }

  const items: PeerAssessmentSubmitItem[] = [];
  for (const criterion of activeCriteria) {
    const source = draft[criterion.criterionId];
    const rawPoints = source?.points ?? stringifyPoints(criterion.peerPoints);
    const normalizedPoints = rawPoints.trim();
    if (!normalizedPoints) {
      return { ok: false, error: 'POINTS_REQUIRED' };
    }

    const points = Number(normalizedPoints);
    if (!Number.isFinite(points) || points < 0 || points > criterion.maxPoints) {
      return { ok: false, error: 'POINTS_OUT_OF_RANGE' };
    }

    items.push({
      criterionId: criterion.criterionId,
      points,
      comment: (source?.comment ?? criterion.peerComment ?? '').trim(),
    });
  }

  return { ok: true, items };
}

export function getPeerAssessmentValidationMessage(error: string): string {
  switch (error) {
    case 'NO_ACTIVE_CRITERIA':
      return 'Нет активных критериев для peer-оценивания.';
    case 'POINTS_REQUIRED':
      return 'Заполните баллы по каждому критерию.';
    case 'POINTS_OUT_OF_RANGE':
      return 'Баллы должны быть в пределах критерия.';
    default:
      return 'Проверьте форму peer-оценивания.';
  }
}

function sortPeerCriteria(criteria: PeerAssessmentCriterion[]): PeerAssessmentCriterion[] {
  return [...criteria].sort((left, right) => left.orderIndex - right.orderIndex);
}

function stringifyPoints(points: number | null): string {
  return points === null ? '' : String(points);
}
