export type TeacherTaskDetailsSection = 'overview' | 'submissions' | 'comments';
export type TaskAssignmentType = 'INDIVIDUAL' | 'TEAM';
export type TaskTeamType = 'RANDOM' | 'FREEROAM' | 'DRAFT' | 'CUSTOM';
export type TaskResolveType =
  | 'FIRST_SUBMITTED_SOLUTION'
  | 'LAST_SUBMITTED_SOLUTION'
  | 'CAPTAINS_SOLUTION'
  | 'MOST_VOTES_SOLUTION'
  | 'AT_LEAST_VOTES_SOLUTION';

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
  assignmentType: TaskAssignmentType;
  teamType: TaskTeamType;
  resolveType: TaskResolveType;
  minTeamSize: number | null;
  maxTeamSize: number | null;
  minTeamsAmount: number | null;
  maxTeamsAmount: number | null;
  votesThreshold: number | null;
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

export type TeacherStudentGrade = {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  grade: string;
  saving: boolean;
  error: string | null;
};

export type CreateTaskPayload = {
  title: string;
  description: string;
  dueDate: string;
  groupId: number;
  groupName: string;
  assignmentType: TaskAssignmentType;
  teamType: TaskTeamType;
  resolveType: TaskResolveType;
  minTeamSize: number | null;
  maxTeamSize: number | null;
  minTeamsAmount: number | null;
  maxTeamsAmount: number | null;
  votesThreshold: number | null;
};

export type CreateNotificationPayload = {
  title: string;
  content: string;
  groupId: number;
  attachmentFile?: File | null;
};
