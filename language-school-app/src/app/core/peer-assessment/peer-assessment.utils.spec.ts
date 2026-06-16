import { describe, expect, it } from 'vitest';
import {
  PeerAssessmentCriterion,
  PeerAssessmentTaskContext,
} from './peer-assessment.contracts';
import {
  buildPeerAssessmentItems,
  createMockPeerAssessment,
  selectAssignedPeerTeam,
} from './peer-assessment.utils';

describe('Feature: Peer-assessment assignment', () => {
  it('Scenario: captain is assigned to review another team', () => {
    const task: PeerAssessmentTaskContext = {
      id: 'task-1',
      currentTeamId: 'team-a',
      teams: [
        { id: 'team-a', name: 'Alpha', captainId: 'student-1' },
        { id: 'team-b', name: 'Beta', captainId: 'student-2' },
      ],
    };

    const assignedTeam = selectAssignedPeerTeam(task, 'student-1');

    expect(assignedTeam?.name).toBe('Beta');
  });

  it('Scenario: non-captain does not see peer-assessment assignment', () => {
    const task: PeerAssessmentTaskContext = {
      id: 'task-1',
      currentTeamId: 'team-a',
      teams: [
        { id: 'team-a', name: 'Alpha', captainId: 'student-1' },
        { id: 'team-b', name: 'Beta', captainId: 'student-2' },
      ],
    };

    const assignedTeam = selectAssignedPeerTeam(task, 'student-3');

    expect(assignedTeam).toBeNull();
  });
});

describe('Feature: Peer-assessment submit payload', () => {
  it('Scenario: captain fills points and comments for each criterion', () => {
    const criteria = createCriteria();

    const result = buildPeerAssessmentItems(criteria, {
      content: { points: '4', comment: 'Clear answer' },
      language: { points: '5', comment: 'Accurate language' },
    });

    expect(result).toEqual({
      ok: true,
      items: [
        { criterionId: 'content', points: 4, comment: 'Clear answer' },
        { criterionId: 'language', points: 5, comment: 'Accurate language' },
      ],
    });
  });

  it('Scenario: captain cannot submit without points for every active criterion', () => {
    const result = buildPeerAssessmentItems(createCriteria(), {
      content: { points: '4', comment: '' },
    });

    expect(result).toEqual({ ok: false, error: 'POINTS_REQUIRED' });
  });

  it('Scenario: captain cannot submit points above criterion maximum', () => {
    const result = buildPeerAssessmentItems(createCriteria(), {
      content: { points: '6', comment: '' },
      language: { points: '5', comment: '' },
    });

    expect(result).toEqual({ ok: false, error: 'POINTS_OUT_OF_RANGE' });
  });

  it('Scenario: saved peer-assessment shows target team and total score', () => {
    const assessment = createMockPeerAssessment(
      'task-1',
      { id: 'team-b', name: 'Beta', captainId: 'student-2' },
      [
        { ...createCriterion('content', 1), peerPoints: 4 },
        { ...createCriterion('language', 2), peerPoints: 5 },
      ],
    );

    expect(assessment.targetTeamName).toBe('Beta');
    expect(assessment.peerTotal).toBe(9);
    expect(assessment.totalMaxPoints).toBe(10);
  });
});

function createCriteria(): PeerAssessmentCriterion[] {
  return [
    createCriterion('language', 2),
    createCriterion('content', 1),
  ];
}

function createCriterion(criterionId: string, orderIndex: number): PeerAssessmentCriterion {
  return {
    criterionId,
    title: criterionId,
    description: `${criterionId} description`,
    maxPoints: 5,
    sectionName: 'Peer review',
    orderIndex,
    active: true,
    peerPoints: null,
    peerComment: null,
  };
}
