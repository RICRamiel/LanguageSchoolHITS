import { NotificationAttachment } from '../../core/teacher/teacher.models';
import { ParticipationAssessment, ParticipationAssessmentItem } from '../../core/teacher/teacher.models';
import { StudentPeerAssessment } from '../../core/peer-assessment/peer-assessment.contracts';

export type StudentTeam = {
  id: string;
  name: string;
  membersCount: number | null;
  captainId: string | null;
};

export type StudentTask = {
  id: string;
  participationId: string | null;
  title: string;
  pillText: string | null;
  pillVariant: 'success' | 'neutral';
  teacher: string;
  description: string;
  dueText: string;
  groupId: string | null;
  teamType: string | null;
  maxTeamSize: number | null;
  currentTeamId: string | null;
  teams: StudentTeam[];
  attachedWorks: NotificationAttachment[];
};

export type StudentTaskComment = {
  userId: string | null;
  text: string;
};

export type StudentNotification = {
  id: string;
  type: 'announcement' | 'task' | 'comment';
  title: string;
  author: string;
  dateTime: string;
  text: string;
  tag: string;
  attachments: NotificationAttachment[];
  groupId: string;
};

export type StudentParticipationAssessment = ParticipationAssessment;
export type StudentParticipationAssessmentItem = ParticipationAssessmentItem;
export type StudentTaskPeerAssessment = StudentPeerAssessment;
