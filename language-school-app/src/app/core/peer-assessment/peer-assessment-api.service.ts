import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { AssessmentItemDTO, PeerReviewAccessDTO, TaskCriterionDTO } from '../../api/model/models';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';
import {
  PeerAssessmentCriterion,
  PeerAssessmentSubmitItem,
  StudentPeerAssessment,
} from './peer-assessment.contracts';

@Injectable({ providedIn: 'root' })
export class PeerAssessmentApiService {
  private readonly http = inject(HttpClient);

  getAssignedPeerAssessment(taskId: string): Observable<StudentPeerAssessment> {
    const normalizedTaskId = taskId.trim();
    return this.http
      .get<PeerReviewAccessDTO>(withOpenApiBase(OPENAPI_PATHS.tasks.myPeerReviewAssignment(normalizedTaskId)))
      .pipe(map((access) => this.mapPeerReviewAccess(access, taskId.trim())));
  }

  submitPeerAssessment(
    taskId: string,
    items: PeerAssessmentSubmitItem[],
  ): Observable<StudentPeerAssessment> {
    const normalizedTaskId = taskId.trim();
    return this.http
      .post<PeerReviewAccessDTO>(
        withOpenApiBase(OPENAPI_PATHS.tasks.submitMyPeerReviewAssignment(normalizedTaskId)),
        { items },
      )
      .pipe(map((access) => this.mapPeerReviewAccess(access, taskId.trim())));
  }

  private mapPeerReviewAccess(access: PeerReviewAccessDTO, fallbackTaskId: string): StudentPeerAssessment {
    const assessmentItems = new Map(
      (access.assessment?.items ?? [])
        .map((item) => [this.asString(item.criterionId), item] as const)
        .filter(([criterionId]) => Boolean(criterionId)),
    );
    const criteria = (access.criteria ?? [])
      .map((criterion) => this.mapCriterion(criterion, assessmentItems.get(this.asString(criterion.id))))
      .sort((left, right) => left.orderIndex - right.orderIndex);
    const activeCriteria = criteria.filter((criterion) => criterion.active);
    const totalMaxPoints = this.finiteNumber(access.totalMaxPoints)
      ?? this.finiteNumber(access.assessment?.totalMaxPoints)
      ?? activeCriteria.reduce((total, criterion) => total + criterion.maxPoints, 0);

    return {
      taskId: this.asString(access.taskId) || fallbackTaskId,
      targetTeamId: this.asString(access.reviewedTeamId),
      targetTeamName: this.asString(access.reviewedTeamName) || 'Команда не указана',
      targetParticipationId: this.asString(access.targetParticipationId),
      totalMaxPoints,
      peerTotal: this.finiteNumber(access.assessment?.totalPoints),
      submitted: access.canSubmit === false || Boolean(access.assessment),
      criteria,
    };
  }

  private mapCriterion(
    criterion: TaskCriterionDTO,
    assessmentItem: AssessmentItemDTO | undefined,
  ): PeerAssessmentCriterion {
    return {
      criterionId: this.asString(criterion.id ?? assessmentItem?.criterionId),
      title: this.asString(criterion.title ?? assessmentItem?.title) || 'Критерий',
      description: this.asString(criterion.description ?? assessmentItem?.description),
      maxPoints: this.finiteNumber(criterion.maxPoints ?? assessmentItem?.maxPoints) ?? 0,
      sectionName: this.asString(criterion.sectionName ?? assessmentItem?.sectionName),
      orderIndex: this.finiteNumber(criterion.orderIndex ?? assessmentItem?.orderIndex) ?? 0,
      active: criterion.active ?? assessmentItem?.active ?? true,
      peerPoints: this.finiteNumber(assessmentItem?.points),
      peerComment: typeof assessmentItem?.comment === 'string' ? assessmentItem.comment : null,
    };
  }

  private asString(value: unknown): string {
    return typeof value === 'string' ? value.trim() : '';
  }

  private finiteNumber(value: unknown): number | null {
    return typeof value === 'number' && Number.isFinite(value) ? value : null;
  }
}
