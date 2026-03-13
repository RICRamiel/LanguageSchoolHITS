export interface Group {
  id: number;
  name: string;
  language: string;
  teacherName: string;
  studentsCount: number | string;
}

export interface Student {
  id: number;
  fullName: string;
  email: string;
  groupName: string;
  /** IDs групп, в которых состоит студент (для модалки «Привязать к группе») */
  groupIds?: number[];
}

export interface Teacher {
  id: number;
  fullName: string;
  email: string;
  languages: string;
  groupId?: number | null;
  groupName?: string;
}

export interface Language {
  id: number;
  name: string;
}
