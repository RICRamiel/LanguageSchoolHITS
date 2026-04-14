import { NotificationAttachment } from '../../core/teacher/teacher.models';

export type StudentTask = {
  id: string;
  title: string;
  pillText: string | null;
  pillVariant: 'success' | 'neutral';
  teacher: string;
  description: string;
  dueText: string;
  groupId: string | null;
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
