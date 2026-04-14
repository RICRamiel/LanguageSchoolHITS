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
    byCourse: (courseId: number | string) => `/notification/by-course/${encodeURIComponent(String(courseId))}`,
    byGroup: (groupId: number | string) => `/notification/by-group/${encodeURIComponent(String(groupId))}`,
    forStudent: (studentId: number | string) => `/notification/for-students/${encodeURIComponent(String(studentId))}`,
    create: '/notification/create',
  },
  comments: {
    create: '/comment/create',
    byTask: (taskId: number | string) => `/comment/${encodeURIComponent(String(taskId))}/get`,
  },
  attachments: {
    upload: '/attachments',
    uploadToNotification: '/attachments/to-notification',
    download: (attachmentId: number | string) => `/attachments/${attachmentId}/download`,
  },
  tasks: {
    byCourse: (courseId: number | string) => `/task/course/${encodeURIComponent(String(courseId))}`,
    byGroupName: (groupName: string) =>
      `/task/get_by_group_name?groupName=${encodeURIComponent(groupName)}`,
    byGroupNameReal: (groupName: string) =>
      `/task/get_by_group_name_real?groupName=${encodeURIComponent(groupName)}`,
  },
  teacher: {
    tasksByTeacher: (teacherId: number | string) => `/task/${encodeURIComponent(String(teacherId))}/get_by_teacher`,
    createTask: '/task/create',
    groupsByTeacher: (teacherId: number | string) => `/group/${encodeURIComponent(String(teacherId))}/get_groups_by_teacher`,
    groupsWithFilters: '/group/get-groups-with-filters',
    commentsByTask: (taskId: number | string) => `/comment/${encodeURIComponent(String(taskId))}/get`,
  },
  courses: {
    list: '/course',
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
      listByGroup: (groupId: number | string) => `/api/users/students?groupId=${encodeURIComponent(String(groupId))}`,
      create: '/api/users/students',
      get: (id: number | string) => `/api/users/students/${encodeURIComponent(String(id))}`,
      update: (id: number | string) => `/api/users/students/${encodeURIComponent(String(id))}`,
      delete: (id: number | string) => `/api/users/students/${encodeURIComponent(String(id))}`,
    },
    teachers: {
      list: '/api/users/teachers',
      create: '/api/users/teachers', // POST to same path
      get: (id: number | string) => `/api/users/teachers/${encodeURIComponent(String(id))}`,
      update: (id: number | string) => `/api/users/teachers/${encodeURIComponent(String(id))}`,
      delete: (id: number | string) => `/api/users/teachers/${encodeURIComponent(String(id))}`,
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
