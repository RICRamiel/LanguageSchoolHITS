import { API_URL } from '../../../secrets';

const OPENAPI_SERVER_URL = 'http://api.thallassianangel.su:5461';

export const OPENAPI_PATHS = {
  auth: {
    login: '/auth/login',
    logout: '/auth/logout',
  },
  users: {
    me: '/api/users/me',
  },
  notifications: {
    byCourse: (courseId: string) => `/notification/by-course/${encodeURIComponent(courseId)}`,
    forStudent: (studentId: string) => `/notification/for-students/${encodeURIComponent(studentId)}`,
    create: '/notification/create',
  },
  comments: {
    create: '/comment/create',
    byTask: (taskId: string) => `/comment/${encodeURIComponent(taskId)}/get`,
  },
  attachments: {
    upload: '/attachments',
    uploadToNotification: '/attachments/to-notification',
    download: (attachmentId: string | number) => `/attachments/${attachmentId}/download`,
  },
  tasks: {
    byCourse: (courseId: string) => `/task/course/${encodeURIComponent(courseId)}`,
    byGroupName: (groupName: string) =>
      `/task/get_by_group_name?groupName=${encodeURIComponent(groupName)}`,
    byGroupNameReal: (groupName: string) =>
      `/task/get_by_group_name_real?groupName=${encodeURIComponent(groupName)}`,
    teams: (taskId: string) => `/task/${encodeURIComponent(taskId)}/teams`,
    joinTeam: (taskId: string, teamId: string) =>
      `/task/${encodeURIComponent(taskId)}/teams/${encodeURIComponent(teamId)}/join`,
    addStudentToTeam: (taskId: string, teamId: string, studentId: string) =>
      `/task/${encodeURIComponent(taskId)}/teams/${encodeURIComponent(teamId)}/students/${encodeURIComponent(studentId)}`,
  },
  teacher: {
    tasksByTeacher: (teacherId: string | number) => `/task/${encodeURIComponent(String(teacherId))}/get_by_teacher`,
    createTask: '/task/create',
    commentsByTask: (taskId: string) => `/comment/${encodeURIComponent(taskId)}/get`,
  },
  courses: {
    list: '/course',
  },
  admin: {
    languages: {
      list: '/language/get_all_languages',
      create: '/language/create',
      edit: (id: string) => `/language/${id}/edit`,
      delete: (id: string) => `/language/${id}/delete`,
    },
    groups: {
      list: '/course',
      create: '/course/create',
      edit: (id: string) => `/course/${id}/edit`,
      delete: '/course/delete',
      addStudents: '/course/addStudents',
    },
    students: {
      list: '/api/users/students',
      listByGroup: (groupId: string) => `/api/users/students?groupId=${encodeURIComponent(groupId)}`,
      create: '/api/users/students',
      update: (id: string) => `/api/users/students/${encodeURIComponent(id)}`,
      delete: (id: string) => `/api/users/students/${encodeURIComponent(id)}`,
    },
    teachers: {
      list: '/api/users/teachers',
      create: '/api/users/teachers',
      update: (id: string) => `/api/users/teachers/${encodeURIComponent(id)}`,
      delete: (id: string) => `/api/users/teachers/${encodeURIComponent(id)}`,
    },
  },
} as const;

function normalizeBaseUrl(url: string): string {
  return url.replace(/\/+$/, '');
}

export function withOpenApiBase(path: string): string {
  const baseUrl = normalizeBaseUrl(API_URL || OPENAPI_SERVER_URL);
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${baseUrl}${normalizedPath}`;
}
