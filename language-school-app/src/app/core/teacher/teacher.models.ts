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
  taskId: string;
  section: TeacherTaskDetailsSection;
};

export type TeacherTaskSubmission = {
  id: string | number | null;
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
  grade: string;
  saving: boolean;
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
