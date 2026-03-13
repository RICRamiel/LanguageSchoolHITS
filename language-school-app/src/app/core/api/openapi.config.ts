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
  notifications: {
    byGroup: (groupId: number) => `/notification/by-group/${groupId}`,
    forStudent: (studentId: number) => `/notification/for-students/${studentId}`,
    create: '/notification/create',
  },
  comments: {
    create: '/comment/create',
    byTask: (taskId: number) => `/comment/${taskId}/get`,
  },
  attachments: {
    upload: '/attachments',
    uploadToNotification: '/attachments/to-notification',
    download: (attachmentId: number | string) => `/attachments/${attachmentId}/download`,
  },
  tasks: {
    byGroupName: (groupName: string) =>
      `/task/get_by_group_name?groupName=${encodeURIComponent(groupName)}`,
  },
  teacher: {
    tasksByTeacher: (teacherId: number) => `/task/${teacherId}/get_by_teacher`,
    createTask: '/task/create',
    groupsByTeacher: (teacherId: number) => `/group/${teacherId}/get_groups_by_teacher`,
    groupsWithFilters: '/group/get-groups-with-filters',
    commentsByTask: (taskId: number) => `/comment/${taskId}/get`,
  },
  admin: {
    languages: {
      list: '/language/get_all_languages',
      create: '/language/create',
      edit: (id: number) => `/language/${id}/edit`,
      delete: (id: number) => `/language/${id}/delete`,
    },
    groups: {
      list: '/group/get_all_groups',
      create: '/group/create',
      edit: (id: number) => `/group/${id}/edit`,
      delete: (groupId: number) => `/group/${groupId}/delete`,
      addStudent: (groupId: number, userId: number) => `/group/${groupId}/add/${userId}`,
      removeStudent: (groupId: number, userId: number) => `/group/${groupId}/add/${userId}`,
    },
    students: {
      list: '/api/users/students',
      listByGroup: (groupId: number) => `/api/users/students?groupId=${groupId}`,
      create: '/api/users/students',
      get: (id: number) => `/api/users/students/${id}`,
      update: (id: number) => `/api/users/students/${id}`,
      delete: (id: number) => `/api/users/students/${id}`,
    },
    teachers: {
      list: '/api/users/teachers',
      create: '/api/users/teachers', // POST to same path
      get: (id: number) => `/api/users/teachers/${id}`,
      update: (id: number) => `/api/users/teachers/${id}`,
      delete: (id: number) => `/api/users/teachers/${id}`,
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
