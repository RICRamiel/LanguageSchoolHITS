import { NotificationAttachment } from '../../core/teacher/teacher.models';

export type StudentTeam = {
  id: string;
  name: string;
  membersCount: number | null;
  captainId: string | null;
};

export type StudentTask = {
  id: string;
  title: string;
  pillText: string | null;
  pillVariant: 'success' | 'neutral';
  teacher: string;
  description: string;
  dueText: string;
  groupId: string | null;
  teamType: string | null;
  currentTeamId: string | null;
  teams: StudentTeam[];
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
