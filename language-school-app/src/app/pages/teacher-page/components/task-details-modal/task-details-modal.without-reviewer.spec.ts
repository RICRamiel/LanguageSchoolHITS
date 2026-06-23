import { describe, it, expect } from 'vitest';
import { PeerAssessmentResult } from '../../teacher-page.types';

function teamsWithoutReviewer(results: PeerAssessmentResult[]): string[] {
  return results
    .filter((r) => r.status === 'WITHOUT_REVIEWER')
    .map((r) => r.reviewedTeamName);
}

function makeResult(overrides: Partial<PeerAssessmentResult> = {}): PeerAssessmentResult {
  return {
    id: 'assign-1',
    assignmentId: 'assign-1',
    assessmentId: 'assessment-1',
    reviewedTeamId: 'team-a',
    reviewedTeamName: 'Team A',
    reviewerTeamId: 'team-b',
    reviewerTeamName: 'Team B',
    status: 'SUBMITTED',
    submittedAt: '2026-06-01T10:00:00',
    totalPoints: 8,
    totalMaxPoints: 10,
    criteria: [],
    ...overrides,
  };
}

function makeWithoutReviewerResult(reviewedTeamName: string): PeerAssessmentResult {
  return makeResult({
    id: `assign-${reviewedTeamName}`,
    assignmentId: `assign-${reviewedTeamName}`,
    assessmentId: null,
    reviewedTeamId: 'team-no-reviewer',
    reviewedTeamName,
    reviewerTeamId: null,
    reviewerTeamName: null,
    status: 'WITHOUT_REVIEWER',
    submittedAt: null,
    totalPoints: null,
  });
}

describe('Feature: Уведомление о командах без оценщика', () => {
  it('Scenario: нет команд без оценщика — список пуст', () => {
    const results = [makeResult()];

    expect(teamsWithoutReviewer(results)).toEqual([]);
  });

  it('Scenario: одна команда без оценщика — её название в уведомлении', () => {
    const results = [makeResult(), makeWithoutReviewerResult('Team C')];

    expect(teamsWithoutReviewer(results)).toEqual(['Team C']);
  });

  it('Scenario: несколько команд без оценщика — все отображаются', () => {
    const results = [
      makeWithoutReviewerResult('Team X'),
      makeWithoutReviewerResult('Team Y'),
    ];

    expect(teamsWithoutReviewer(results)).toEqual(['Team X', 'Team Y']);
  });

  it('Scenario: только WITHOUT_REVIEWER попадают в уведомление, остальные статусы — нет', () => {
    const results = [
      makeResult({ status: 'SUBMITTED', reviewedTeamName: 'Team B' }),
      makeResult({ status: 'FINAL', reviewedTeamName: 'Team D' }),
      makeWithoutReviewerResult('Одинокая команда'),
    ];

    const names = teamsWithoutReviewer(results);
    expect(names).toContain('Одинокая команда');
    expect(names).not.toContain('Team B');
    expect(names).not.toContain('Team D');
  });

  it('Scenario: пустой список результатов — уведомление не нужно', () => {
    expect(teamsWithoutReviewer([])).toEqual([]);
  });
});
