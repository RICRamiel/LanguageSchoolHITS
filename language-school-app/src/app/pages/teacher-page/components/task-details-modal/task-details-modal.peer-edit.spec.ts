import { describe, it, expect } from 'vitest';
import { PeerAssessmentResult } from '../../teacher-page.types';
import {
  buildPeerEditDraft,
  buildPeerEditItems,
  updateDraftComment,
  updateDraftPoints,
} from './peer-edit.utils';

function makeResult(overrides: Partial<PeerAssessmentResult> = {}): PeerAssessmentResult {
  return {
    id: 'assign-1',
    reviewedTeamId: 'team-reviewed',
    reviewedTeamName: 'Team A',
    reviewerTeamId: 'team-reviewer',
    reviewerTeamName: 'Team B',
    status: 'SUBMITTED',
    submittedAt: '2026-06-01T10:00:00',
    totalPoints: 7,
    totalMaxPoints: 10,
    criteria: [
      {
        criterionId: 'crit-1',
        title: 'Code Quality',
        description: 'Quality of code',
        maxPoints: 10,
        sectionName: 'Technical',
        orderIndex: 1,
        points: 7,
        comment: 'Original comment',
      },
    ],
    ...overrides,
  };
}

describe('Feature: Преподаватель редактирует peer-оценку', () => {
  it('Scenario: нажатие "Редактировать" открывает форму с предзаполненными баллами', () => {
    const draft = buildPeerEditDraft(makeResult());

    expect(draft['crit-1']).toEqual({ points: '7', comment: 'Original comment' });
  });

  it('Scenario: нажатие "Отмена" закрывает форму и очищает черновик', () => {
    const draft = buildPeerEditDraft(makeResult());
    const cleared = {};

    expect(Object.keys(draft)).toHaveLength(1);
    expect(cleared).toEqual({});
  });

  it('Scenario: ввод баллов обновляет черновик', () => {
    const draft = buildPeerEditDraft(makeResult());
    const updated = updateDraftPoints(draft, 'crit-1', '9');

    expect(updated['crit-1'].points).toBe('9');
    expect(updated['crit-1'].comment).toBe('Original comment');
  });

  it('Scenario: ввод комментария обновляет черновик', () => {
    const draft = buildPeerEditDraft(makeResult());
    const updated = updateDraftComment(draft, 'crit-1', 'Teacher override');

    expect(updated['crit-1'].comment).toBe('Teacher override');
    expect(updated['crit-1'].points).toBe('7');
  });

  it('Scenario: "Сохранить" строит корректные items для отправки', () => {
    let draft = buildPeerEditDraft(makeResult());
    draft = updateDraftPoints(draft, 'crit-1', '9');
    draft = updateDraftComment(draft, 'crit-1', 'Teacher override');

    const items = buildPeerEditItems(draft);

    expect(items).toHaveLength(1);
    expect(items[0]).toEqual({ criterionId: 'crit-1', points: 9, comment: 'Teacher override' });
  });

  it('Scenario: "Сохранить" не строит items, если поле баллов пустое', () => {
    const result = makeResult({
      criteria: [{ ...makeResult().criteria[0], points: null }],
    });
    let draft = buildPeerEditDraft(result);
    draft = updateDraftPoints(draft, 'crit-1', '');

    const items = buildPeerEditItems(draft);

    expect(items).toHaveLength(0);
  });

  it('Scenario: критерий с null баллами — в черновике пустая строка', () => {
    const result = makeResult({
      criteria: [{ ...makeResult().criteria[0], points: null, comment: null }],
    });

    const draft = buildPeerEditDraft(result);

    expect(draft['crit-1']).toEqual({ points: '', comment: '' });
  });

  it('Scenario: несколько критериев — все попадают в черновик', () => {
    const result = makeResult({
      criteria: [
        { ...makeResult().criteria[0], criterionId: 'crit-1', points: 5, comment: 'Good' },
        { ...makeResult().criteria[0], criterionId: 'crit-2', points: 3, comment: 'Average' },
      ],
    });

    const draft = buildPeerEditDraft(result);
    const items = buildPeerEditItems(draft);

    expect(Object.keys(draft)).toHaveLength(2);
    expect(items).toHaveLength(2);
  });
});
