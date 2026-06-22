import { describe, it, expect, beforeEach } from 'vitest';
import { MockPeerAssessmentApiService } from './mock-peer-assessment-api.service';
import { PeerAssessmentTaskContext } from './peer-assessment.contracts';

const TASK: PeerAssessmentTaskContext = {
  id: 'task-1',
  currentTeamId: 'team-a',
  teams: [
    { id: 'team-a', name: 'Alpha', captainId: 'captain-1' },
    { id: 'team-b', name: 'Beta', captainId: 'captain-2' },
  ],
};

const SUBMIT_ITEMS = [
  { criterionId: 'peer-content', points: 4, comment: 'Good content' },
  { criterionId: 'peer-language', points: 5, comment: 'Excellent language' },
  { criterionId: 'peer-presentation', points: 3, comment: 'Average presentation' },
];

describe('Feature: Отправка peer-оценки капитаном', () => {
  let service: MockPeerAssessmentApiService;

  beforeEach(() => {
    service = new MockPeerAssessmentApiService();
  });

  it('Scenario: до отправки — оценка не помечена как отправленная', async () => {
    let assessment = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));

    expect(assessment).not.toBeNull();
    expect(assessment!.submitted).toBe(false);
  });

  it('Scenario: капитан отправляет peer-оценку — помечается как отправленная', async () => {
    const initial = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));
    expect(initial).not.toBeNull();

    const submitted = await firstValue(
      service.submitPeerAssessment(TASK.id, initial!.targetParticipationId, SUBMIT_ITEMS, initial!),
    );

    expect(submitted.submitted).toBe(true);
  });

  it('Scenario: система сохраняет оценку — баллы по критериям сохранены', async () => {
    const initial = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));

    const submitted = await firstValue(
      service.submitPeerAssessment(TASK.id, initial!.targetParticipationId, SUBMIT_ITEMS, initial!),
    );

    const contentCriterion = submitted.criteria.find((c) => c.criterionId === 'peer-content');
    expect(contentCriterion?.peerPoints).toBe(4);
    expect(contentCriterion?.peerComment).toBe('Good content');
  });

  it('Scenario: после отправки итоговый балл рассчитан', async () => {
    const initial = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));

    const submitted = await firstValue(
      service.submitPeerAssessment(TASK.id, initial!.targetParticipationId, SUBMIT_ITEMS, initial!),
    );

    expect(submitted.peerTotal).toBe(12);
  });

  it('Scenario: капитан не может изменить отправленную peer-оценку — повторная загрузка возвращает submitted=true', async () => {
    const initial = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));

    await firstValue(
      service.submitPeerAssessment(TASK.id, initial!.targetParticipationId, SUBMIT_ITEMS, initial!),
    );

    const reloaded = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));

    expect(reloaded!.submitted).toBe(true);
  });

  it('Scenario: обычный участник (не капитан) не видит peer-оценку', async () => {
    const assessment = await firstValue(service.getAssignedPeerAssessment(TASK, 'not-a-captain'));

    expect(assessment).toBeNull();
  });

  it('Scenario: команда не оценивает саму себя — назначенная команда не равна команде капитана', async () => {
    const assessment = await firstValue(service.getAssignedPeerAssessment(TASK, 'captain-1'));

    expect(assessment).not.toBeNull();
    expect(assessment!.targetTeamId).not.toBe(TASK.currentTeamId);
  });
});

function firstValue<T>(obs: import('rxjs').Observable<T>): Promise<T> {
  return new Promise((resolve, reject) => {
    obs.subscribe({ next: resolve, error: reject });
  });
}
