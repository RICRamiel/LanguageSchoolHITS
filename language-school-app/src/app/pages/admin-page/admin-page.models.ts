export interface Group {
  id: string;
  name: string;
  language: string;
  teacherName: string;
  studentsCount: number | string;
}

export interface Student {
  id: string;
  fullName: string;
  email: string;
  groupName: string;
  /** IDs курсов, в которых состоит студент (для модалки «Привязать к курсу») */
  groupIds?: string[];
}

export interface Teacher {
  id: string;
  fullName: string;
  email: string;
  languages: string;
  groupId?: string | null;
  groupName?: string;
}

export interface Language {
  id: string;
  name: string;
}
