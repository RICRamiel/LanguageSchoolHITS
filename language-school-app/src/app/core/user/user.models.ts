export type UserMeResponse = {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ADMIN' | 'TEACHER' | 'STUDENT';
  groups: Array<{
    id: string;
    name: string;
  }>;
};
