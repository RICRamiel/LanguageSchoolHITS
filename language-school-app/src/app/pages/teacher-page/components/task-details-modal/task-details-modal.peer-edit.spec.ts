import { TestBed } from '@angular/core/testing';
import { describe, beforeEach, it, expect, vi } from 'vitest';
import { TaskDetailsModalComponent } from './task-details-modal.component';
import { PeerAssessmentResult } from '../../teacher-page.types';

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

function makeTask(): TaskDetailsModalComponent['task'] extends (...args: any[]) => infer R ? R : never {
  return {
    id: 'task-1',
    title: 'Test Task',
    description: 'Desc',
    peerReviewEnabled: true,
    assignmentType: 'TEAM',
    teamType: null,
    resolveType: null,
    minTeamsAmount: null,
    maxTeamsAmount: null,
    votesThreshold: null,
    submissionClosed: false,
    teams: [],
    taskComments: [],
    attachedWorks: [],
    dueDate: null,
    courseId: 'course-1',
    groupName: 'Group A',
  } as any;
}

describe('Feature: Преподаватель редактирует peer-оценку (TaskDetailsModalComponent)', () => {
  let component: TaskDetailsModalComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskDetailsModalComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(TaskDetailsModalComponent);
    fixture.componentRef.setInput('task', makeTask());
    fixture.detectChanges();
    component = fixture.componentInstance;
  });

  it('Scenario: нажатие "Редактировать" открывает форму с предзаполненными баллами', () => {
    const result = makeResult();

    component.startEditPeerResult(result);

    expect(component.editingPeerResultId()).toBe('assign-1');
    expect(component.peerEditDraft()['crit-1']).toEqual({ points: '7', comment: 'Original comment' });
  });

  it('Scenario: нажатие "Отмена" закрывает форму и очищает черновик', () => {
    component.startEditPeerResult(makeResult());

    component.cancelEditPeerResult();

    expect(component.editingPeerResultId()).toBeNull();
    expect(component.peerEditDraft()).toEqual({});
  });

  it('Scenario: ввод баллов обновляет черновик', () => {
    component.startEditPeerResult(makeResult());

    const event = { target: { value: '9' } } as unknown as Event;
    component.onPeerEditPointsInput('crit-1', event);

    expect(component.peerEditDraft()['crit-1'].points).toBe('9');
    expect(component.peerEditDraft()['crit-1'].comment).toBe('Original comment');
  });

  it('Scenario: ввод комментария обновляет черновик', () => {
    component.startEditPeerResult(makeResult());

    const event = { target: { value: 'Teacher override' } } as unknown as Event;
    component.onPeerEditCommentInput('crit-1', event);

    expect(component.peerEditDraft()['crit-1'].comment).toBe('Teacher override');
    expect(component.peerEditDraft()['crit-1'].points).toBe('7');
  });

  it('Scenario: "Сохранить" эмитит editPeerAssessment с корректными данными', () => {
    component.startEditPeerResult(makeResult());
    const pointsEvent = { target: { value: '9' } } as unknown as Event;
    component.onPeerEditPointsInput('crit-1', pointsEvent);
    const commentEvent = { target: { value: 'Teacher override' } } as unknown as Event;
    component.onPeerEditCommentInput('crit-1', commentEvent);

    const emitted: { assignmentId: string; items: any[] }[] = [];
    const sub = component.editPeerAssessment.subscribe((v) => emitted.push(v));

    component.submitPeerEdit('assign-1');
    sub.unsubscribe();

    expect(emitted).toHaveLength(1);
    expect(emitted[0].assignmentId).toBe('assign-1');
    expect(emitted[0].items).toEqual([
      { criterionId: 'crit-1', points: 9, comment: 'Teacher override' },
    ]);
  });

  it('Scenario: "Сохранить" не эмитит, если поле баллов пустое', () => {
    component.startEditPeerResult(makeResult({ criteria: [{ ...makeResult().criteria[0], points: null }] }));
    const event = { target: { value: '' } } as unknown as Event;
    component.onPeerEditPointsInput('crit-1', event);

    const emitted: unknown[] = [];
    const sub = component.editPeerAssessment.subscribe((v) => emitted.push(v));
    component.submitPeerEdit('assign-1');
    sub.unsubscribe();

    expect(emitted).toHaveLength(0);
  });

  it('Scenario: "Сохранить" не эмитит, пока идёт сохранение', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ imports: [TaskDetailsModalComponent] });
    const fixture2 = TestBed.createComponent(TaskDetailsModalComponent);
    fixture2.componentRef.setInput('task', makeTask());
    fixture2.componentRef.setInput('peerAssessmentEditSaving', true);
    fixture2.detectChanges();
    const comp2 = fixture2.componentInstance;

    comp2.startEditPeerResult(makeResult());
    const emitted: unknown[] = [];
    const sub = comp2.editPeerAssessment.subscribe((v) => emitted.push(v));
    comp2.submitPeerEdit('assign-1');
    sub.unsubscribe();

    expect(emitted).toHaveLength(0);
  });

  it('Scenario: критерий с null баллами — в черновике пустая строка', () => {
    const result = makeResult({
      criteria: [{ ...makeResult().criteria[0], points: null, comment: null }],
    });

    component.startEditPeerResult(result);

    expect(component.peerEditDraft()['crit-1']).toEqual({ points: '', comment: '' });
  });
});
