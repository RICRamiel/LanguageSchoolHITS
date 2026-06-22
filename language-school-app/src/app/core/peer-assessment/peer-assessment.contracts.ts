export type PeerAssessmentTeamContext = {
  id: string;
  name: string;
  captainId: string | null;
};

export type PeerAssessmentTaskContext = {
  id: string;
  currentTeamId: string | null;
  teams: PeerAssessmentTeamContext[];
};

export type PeerAssessmentCriterion = {
  criterionId: string;
  title: string;
  description: string;
  maxPoints: number;
  sectionName: string;
  orderIndex: number;
  active: boolean;
  peerPoints: number | null;
  peerComment: string | null;
};

export type StudentPeerAssessment = {
  taskId: string;
  targetTeamId: string;
  targetTeamName: string;
  targetParticipationId: string;
  totalMaxPoints: number;
  peerTotal: number | null;
  submitted: boolean;
  criteria: PeerAssessmentCriterion[];
};

export type PeerAssessmentSubmitItem = {
  criterionId: string;
  points: number;
  comment: string;
};

export type PeerAssessmentSubmitPayload = {
  items: PeerAssessmentSubmitItem[];
};

export type PeerAssessmentDraft = Record<string, { points: string; comment: string }>;

export type PeerAssessmentBuildResult =
  | { ok: true; items: PeerAssessmentSubmitItem[] }
  | { ok: false; error: PeerAssessmentValidationError };

export type PeerAssessmentValidationError =
  | 'NO_ACTIVE_CRITERIA'
  | 'POINTS_REQUIRED'
  | 'POINTS_OUT_OF_RANGE';
