export type StudentTask = {
  id: number;
  title: string;
  pillText: string | null;
  pillVariant: 'success' | 'neutral';
  teacher: string;
  description: string;
  dueText: string;
};

export type StudentNotification = {
  id: number;
  type: 'announcement' | 'task' | 'comment';
  title: string;
  author: string;
  dateTime: string;
  text: string;
  tag: string;
  isNew: boolean;
  isUnread: boolean;
};
