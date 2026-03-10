export type UserMeResponse = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ADMIN' | 'TEACHER' | 'STUDENT';
  groups: Array<{
    id: number;
    name: string;
  }>;
};
