export type TeacherTaskDetailsSection = 'overview' | 'submissions' | 'comments';

export type TaskDetailsOpenPayload = {
  title: string;
  section: TeacherTaskDetailsSection;
};

export type TeacherTaskSubmission = {
  studentName: string;
  fileName: string;
  submittedAt: string;
  content: string;
};

export type TeacherTaskComment = {
  studentName: string;
  text: string;
  createdAt: string;
};

export type TeacherTask = {
  title: string;
  description: string;
  dueDate: string;
  submissions: string;
  comments: string;
  group: string;
  attachedWorks: TeacherTaskSubmission[];
  taskComments: TeacherTaskComment[];
};

export type TeacherNotification = {
  id: number;
  title: string;
  text: string;
  date: string;
};

export type CreateTaskPayload = {
  title: string;
  description: string;
  dueDate: string;
  groupId: string;
};

export type CreateNotificationPayload = {
  title: string;
  content: string;
  groupId: string;
};
