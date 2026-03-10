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
} as const;

function normalizeBaseUrl(url: string): string {
  return url.replace(/\/+$/, '');
}

export function withOpenApiBase(path: string): string {
  const baseUrl = normalizeBaseUrl(API_URL || OPENAPI_SERVER_URL);
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${baseUrl}${normalizedPath}`;
}
