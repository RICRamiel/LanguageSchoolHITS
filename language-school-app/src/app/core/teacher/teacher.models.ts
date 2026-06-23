export type TeacherTaskDetailsSection =
  | 'overview'
  | 'submissions'
  | 'comments'
  | 'teams'
  | 'criteria'
  | 'peerReviews';
export type TaskAssignmentType = 'INDIVIDUAL' | 'TEAM';
export type TaskTeamType = 'RANDOM' | 'FREEROAM' | 'DRAFT' | 'CUSTOM';
export type TaskResolveType =
  | 'FIRST_SUBMITTED_SOLUTION'
  | 'LAST_SUBMITTED_SOLUTION'
  | 'CAPTAINS_SOLUTION'
  | 'MOST_VOTES_SOLUTION'
  | 'AT_LEAST_VOTES_SOLUTION';

export type TaskDetailsOpenPayload = {
  taskId: string;
  section: TeacherTaskDetailsSection;
};

export type TeacherTaskSubmission = {
  id: string | number | null;
  fileName: string;
  fileType: string;
  fileSize: number | null;
  objectKey: string | null;
  studentName: string | null;
  teamName: string | null;
  participationId: string | null;
};

export type TeacherTaskComment = {
  studentName: string;
  text: string;
  createdAt: string;
};

export type TaskTeam = {
  id: string;
  name: string;
  membersCount: number | null;
  captainId: string | null;
  participations: TaskTeamParticipation[];
};

export type TaskTeamParticipation = {
  id: string;
  studentId: string;
  studentName: string;
  mark: number | null;
  attachments: TeacherTaskSubmission[];
};

export type TeacherTask = {
  id: string;
  title: string;
  description: string;
  dueDate: string;
  status: 'COMPLETE' | 'OVERDUE' | 'PENDING';
  teacherName: string;
  submissions: string;
  comments: string;
  group: string;
  attachedWorks: TeacherTaskSubmission[];
  taskComments: TeacherTaskComment[];
  assignmentType: TaskAssignmentType;
  teamType: TaskTeamType;
  resolveType: TaskResolveType;
  minTeamSize: number | null;
  maxTeamSize: number | null;
  minTeamsAmount: number | null;
  maxTeamsAmount: number | null;
  votesThreshold: number | null;
  peerReviewEnabled: boolean;
  teams: TaskTeam[];
};

export type TaskCriterion = {
  id: string;
  taskId: string;
  title: string;
  description: string;
  maxPoints: number;
  sectionName: string;
  orderIndex: number;
  active: boolean;
};

export type TaskCriterionPayload = {
  title: string;
  description: string;
  maxPoints: number;
  sectionName: string;
  orderIndex: number;
};

export type AssessmentType = 'SELF' | 'TEACHER' | 'PEER';

export type AssessmentSubmitItem = {
  criterionId: string;
  points: number;
  comment: string;
};

export type ParticipationAssessmentItem = {
  criterionId: string;
  title: string;
  description: string;
  maxPoints: number;
  sectionName: string;
  orderIndex: number;
  active: boolean;
  points: number | null;
  comment: string | null;
  teacherPoints: number | null;
  selfPoints: number | null;
  teacherComment: string | null;
  selfComment: string | null;
};

export type AssessmentDetails = {
  id: string;
  type: AssessmentType;
  totalPoints: number;
  totalMaxPoints: number;
  items: ParticipationAssessmentItem[];
};

export type ParticipationAssessment = {
  taskId: string;
  participationId: string;
  totalMaxPoints: number;
  teacherTotal: number | null;
  selfTotal: number | null;
  criteria: ParticipationAssessmentItem[];
  teacherAssessment: AssessmentDetails | null;
  selfAssessment: AssessmentDetails | null;
};

export type PeerAssessmentCriterionResult = {
  criterionId: string;
  title: string;
  description: string;
  maxPoints: number;
  sectionName: string;
  orderIndex: number;
  points: number | null;
  comment: string | null;
};

export type PeerAssessmentResult = {
  id: string;
  reviewedTeamId: string;
  reviewedTeamName: string;
  reviewerTeamId: string | null;
  reviewerTeamName: string | null;
  status: string;
  submittedAt: string | null;
  totalPoints: number | null;
  totalMaxPoints: number;
  criteria: PeerAssessmentCriterionResult[];
};

export type PeerAssessmentEditItem = {
  criterionId: string;
  points: number;
  comment: string;
};

export type TeacherNotification = {
  id: string;
  title: string;
  text: string;
  date: string;
  groupId: string;
  attachment: NotificationAttachment | null;
};

export type NotificationAttachment = {
  id: string | number | null;
  objectKey: string | null;
  fileName: string;
  fileType: string;
  fileSize: number | null;
};

export type TeacherGroup = {
  id: string;
  name: string;
};

export type TeacherStudentGrade = {
  id: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  error: string | null;
};

export type CreateTaskPayload = {
  title: string;
  description: string;
  dueDate: string;
  groupId: string;
  groupName: string;
  assignmentType: TaskAssignmentType;
  teamType: TaskTeamType;
  resolveType: TaskResolveType;
  minTeamSize: number | null;
  maxTeamSize: number | null;
  minTeamsAmount: number | null;
  maxTeamsAmount: number | null;
  votesThreshold: number | null;
  teamsCreationTimeout: string | null;
};

export type CreateNotificationPayload = {
  title: string;
  content: string;
  groupId: string;
  attachmentFile?: File | null;
};
