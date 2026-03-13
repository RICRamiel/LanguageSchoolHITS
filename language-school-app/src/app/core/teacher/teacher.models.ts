export type TeacherTaskDetailsSection = 'overview' | 'submissions' | 'comments';

export type TaskDetailsOpenPayload = {
  taskId: number;
  section: TeacherTaskDetailsSection;
};

export type TeacherTaskSubmission = {
  id: number | null;
  fileName: string;
  fileType: string;
  fileSize: number | null;
  objectKey: string | null;
};

export type TeacherTaskComment = {
  studentName: string;
  text: string;
  createdAt: string;
};

export type TeacherTask = {
  id: number;
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
};

export type TeacherNotification = {
  id: string;
  title: string;
  text: string;
  date: string;
  groupId: number;
  attachment: NotificationAttachment | null;
};

export type NotificationAttachment = {
  id: number | null;
  objectKey: string | null;
  fileName: string;
  fileType: string;
  fileSize: number | null;
};

export type TeacherGroup = {
  id: number;
  name: string;
};

export type CreateTaskPayload = {
  title: string;
  description: string;
  dueDate: string;
  groupId: number;
  groupName: string;
};

export type CreateNotificationPayload = {
  title: string;
  content: string;
  groupId: number;
  attachmentFile?: File | null;
};
