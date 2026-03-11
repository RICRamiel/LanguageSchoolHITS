import { API_URL } from '../../../secrets';

const OPENAPI_SERVER_URL = 'http://worker.thallassianangel.su';

export const OPENAPI_PATHS = {
  auth: {
    login: '/auth/login',
    logout: '/auth/logout',
  },
  users: {
    me: '/api/users/me',
  },
  teacher: {
    tasksByTeacher: (teacherId: number) => `/task/${teacherId}/get_by_teacher`,
    createTask: '/task/create',
    groupsByTeacher: (teacherId: number) => `/group/${teacherId}/get_groups_by_teacher`,
    commentsByTask: (taskId: number) => `/comment/${taskId}/get`,
    notificationsByGroup: (groupId: number) => `/notification/by-group/${groupId}`,
    createNotification: '/notification/create',
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
