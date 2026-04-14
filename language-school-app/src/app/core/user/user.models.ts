export type UserMeResponse = {
  id: string | number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ADMIN' | 'TEACHER' | 'STUDENT';
  groups: Array<{
    id: string | number;
    name: string;
  }>;
};
