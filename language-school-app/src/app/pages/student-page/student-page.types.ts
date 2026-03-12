import { NotificationAttachment } from '../../core/teacher/teacher.models';

export type StudentTask = {
  id: number;
  title: string;
  pillText: string | null;
  pillVariant: 'success' | 'neutral';
  teacher: string;
  description: string;
  dueText: string;
};

export type StudentTaskComment = {
  userId: number | null;
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
  attachment: NotificationAttachment | null;
};
