export type TeacherTask = {
  title: string;
  description: string;
  dueDate: string;
  submissions: string;
  comments: string;
  group: string;
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
