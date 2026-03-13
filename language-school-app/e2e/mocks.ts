import type { Page, Route } from '@playwright/test';

const API_BASE = 'http://worker.thallassianangel.su';

function json(route: Route, body: unknown, status = 200) {
  return route.fulfill({
    status,
    contentType: 'application/json; charset=utf-8',
    body: JSON.stringify(body),
  });
}

function text(route: Route, body: string, status = 200) {
  return route.fulfill({
    status,
    contentType: 'text/plain; charset=utf-8',
    body,
  });
}

type StudentMockState = {
  commentText: string;
  uploadedTaskIds: number[];
  completedTaskIds: number[];
};

export async function mockStudentApi(page: Page): Promise<StudentMockState> {
  const comments: Array<{ id: number; text: string; userId: number; taskId: number; privateStatus: boolean }> = [];
  let nextCommentId = 1;
  const state: StudentMockState = {
    commentText: '',
    uploadedTaskIds: [],
    completedTaskIds: [],
  };

  await page.route(`${API_BASE}/**`, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();

    if (method === 'GET' && path === '/api/users/me') {
      return json(route, {
        id: 2,
        firstName: 'Student',
        lastName: 'User',
        email: 'student@test.local',
        groups: [{ id: 10, name: 'A1' }],
        role: 'STUDENT',
      });
    }

    if (method === 'GET' && (path === '/task/get_by_group_name' || path === '/task/get_by_group_name_real')) {
      return json(route, [
        {
          id: 2,
          name: 'Тестовое задание',
          description: 'Описание задания',
          deadline: '2026-03-20',
          taskStatus: 'PENDING',
          teacher: { firstName: 'Teacher', lastName: 'Test' },
        },
      ]);
    }

    if (method === 'GET' && path === '/notification/for-students/2') {
      return json(route, []);
    }

    if (method === 'GET' && path === '/comment/2/get') {
      return json(route, comments);
    }

    if (method === 'POST' && path === '/comment/create') {
      const payload = request.postDataJSON() as { text?: string; userId?: number; taskId?: number };
      const created = {
        id: nextCommentId++,
        text: payload.text ?? '',
        userId: payload.userId ?? 2,
        taskId: payload.taskId ?? 2,
        privateStatus: false,
      };
      comments.push(created);
      state.commentText = created.text;
      return json(route, created);
    }

    if (method === 'POST' && path === '/attachments') {
      const taskId = Number(url.searchParams.get('taskId'));
      state.uploadedTaskIds.push(taskId);
      return json(route, { id: 501 });
    }

    if (method === 'GET' && path === '/attachments/501/download') {
      return route.fulfill({
        status: 200,
        contentType: 'application/octet-stream',
        body: Buffer.from('fake-file-content'),
      });
    }

    if (method === 'POST' && path === '/task/2/complete_task') {
      state.completedTaskIds.push(2);
      return text(route, 'OK');
    }

    return route.fulfill({ status: 501, body: `Unhandled mock: ${method} ${path}` });
  });

  return state;
}

type TeacherMockState = {
  notificationCreated: boolean;
  taskCreated: boolean;
  teacherCommentText: string;
};

export async function mockTeacherApi(page: Page): Promise<TeacherMockState> {
  const state: TeacherMockState = {
    notificationCreated: false,
    taskCreated: false,
    teacherCommentText: '',
  };

  const commentsByTask: Record<number, Array<{ id: number; text: string; userId: number; taskId: number; privateStatus: boolean }>> = {
    77: [],
  };
  let nextCommentId = 100;

  await page.route(`${API_BASE}/**`, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();

    if (method === 'GET' && path === '/api/users/me') {
      return json(route, {
        id: 11,
        firstName: 'Teacher',
        lastName: 'User',
        email: 'teacher@test.local',
        groups: [{ id: 10, name: 'A1' }],
        role: 'TEACHER',
      });
    }

    if (method === 'GET' && path === '/group/11/get_groups_by_teacher') {
      return json(route, [{ id: 10, name: 'A1' }]);
    }

    if (method === 'GET' && path === '/task/11/get_by_teacher') {
      return json(route, []);
    }

    if (method === 'GET' && path === '/notification/by-group/10') {
      return json(route, []);
    }

    if (method === 'POST' && path === '/notification/create') {
      const payload = request.postDataJSON() as { text?: string; groupId?: number };
      state.notificationCreated = true;
      return json(route, {
        id: 'n-1',
        text: payload.text ?? '',
        groupId: payload.groupId ?? 10,
        creationDate: '2026-03-11',
        createdByTeacherWithId: 11,
      });
    }

    if (method === 'POST' && path === '/task/create') {
      const payload = request.postDataJSON() as { name?: string; description?: string; deadline?: string };
      state.taskCreated = true;
      return json(route, {
        id: 77,
        name: payload.name ?? 'Новое задание',
        description: payload.description ?? '',
        deadline: payload.deadline ?? '2026-03-20',
        commentList: [],
      });
    }

    if (method === 'GET' && path === '/comment/77/get') {
      return json(route, commentsByTask[77] ?? []);
    }

    if (method === 'POST' && path === '/comment/create') {
      const payload = request.postDataJSON() as { text?: string; userId?: number; taskId?: number };
      const taskId = payload.taskId ?? 77;
      const created = {
        id: nextCommentId++,
        text: payload.text ?? '',
        userId: payload.userId ?? 11,
        taskId,
        privateStatus: false,
      };
      commentsByTask[taskId] = [...(commentsByTask[taskId] ?? []), created];
      state.teacherCommentText = created.text;
      return json(route, created);
    }

    return route.fulfill({ status: 501, body: `Unhandled mock: ${method} ${path}` });
  });

  return state;
}

type AdminMockState = {
  languageCreated: boolean;
  groupCreated: boolean;
  studentCreated: boolean;
  teacherCreated: boolean;
  studentAssignedToGroup: Array<{ groupId: number; userId: number }>;
  teacherAssignedToGroup: Array<{ groupId: number; userId: number }>;
  languageEdited: boolean;
  languageDeleted: boolean;
  groupEdited: boolean;
  groupDeleted: boolean;
  studentUpdated: boolean;
  studentDeleted: boolean;
  teacherUpdated: boolean;
  teacherDeleted: boolean;
};

export async function mockAdminApi(page: Page): Promise<AdminMockState> {
  const state: AdminMockState = {
    languageCreated: false,
    groupCreated: false,
    studentCreated: false,
    teacherCreated: false,
    studentAssignedToGroup: [],
    teacherAssignedToGroup: [],
    languageEdited: false,
    languageDeleted: false,
    groupEdited: false,
    groupDeleted: false,
    studentUpdated: false,
    studentDeleted: false,
    teacherUpdated: false,
    teacherDeleted: false,
  };

  // In-memory data used by admin tabs
  const languages: Array<{ id: number; name: string }> = [
    { id: 1, name: 'Английский' },
  ];

  const groups: Array<{ id: number; name: string; language: { name: string } }> = [
    { id: 10, name: 'A1', language: { name: 'Английский' } },
  ];

  const students: Array<{
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    groups: Array<{ id: number; name: string }>;
  }> = [
    {
      id: 100,
      firstName: 'Student',
      lastName: 'One',
      email: 'student.one@test.local',
      groups: [],
    },
  ];

  const teachers: Array<{
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    groups: Array<{ id: number; name: string }>;
  }> = [
    {
      id: 200,
      firstName: 'Teacher',
      lastName: 'One',
      email: 'teacher.one@test.local',
      groups: [{ id: 10, name: 'A1' }],
    },
  ];

  let nextLanguageId = 1000;
  let nextGroupId = 1000;
  let nextStudentId = 1000;
  let nextTeacherId = 1000;

  await page.route(`${API_BASE}/**`, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();

    // Current user
    if (method === 'GET' && path === '/api/users/me') {
      return json(route, {
        id: 1,
        firstName: 'Admin',
        lastName: 'User',
        email: 'admin@test.local',
        role: 'ADMIN',
      });
    }

    // Languages
    if (method === 'GET' && path === '/language/get_all_languages') {
      return json(route, languages);
    }

    if (method === 'POST' && path === '/language/create') {
      const payload = request.postDataJSON() as { name?: string };
      const created = {
        id: nextLanguageId++,
        name: payload.name ?? 'Новый язык',
      };
      languages.push(created);
      state.languageCreated = true;
      return json(route, created, 201);
    }

    const languageEditMatch = path.match(/^\/language\/(\d+)\/edit$/);
    if (method === 'PUT' && languageEditMatch) {
      const id = Number(languageEditMatch[1]);
      const payload = request.postDataJSON() as { name?: string };
      const lang = languages.find((l) => l.id === id);
      if (lang) {
        lang.name = payload.name ?? lang.name;
        state.languageEdited = true;
        return json(route, lang);
      }
      return route.fulfill({ status: 404, body: 'Language not found' });
    }

    const languageDeleteMatch = path.match(/^\/language\/(\d+)\/delete$/);
    if (method === 'DELETE' && languageDeleteMatch) {
      const id = Number(languageDeleteMatch[1]);
      const idx = languages.findIndex((l) => l.id === id);
      if (idx !== -1) {
        languages.splice(idx, 1);
        state.languageDeleted = true;
      }
      return route.fulfill({ status: 204, body: '' });
    }

    // Groups
    if (method === 'GET' && path === '/group/get_all_groups') {
      return json(route, groups);
    }

    if (method === 'POST' && path === '/group/create') {
      const payload = request.postDataJSON() as { name?: string; language?: { name?: string } };
      const created = {
        id: nextGroupId++,
        name: payload.name ?? 'Новая группа',
        language: { name: payload.language?.name ?? 'Английский' },
      };
      groups.push(created);
      state.groupCreated = true;
      return json(route, created, 201);
    }

    const groupEditMatch = path.match(/^\/group\/(\d+)\/edit$/);
    if (method === 'PUT' && groupEditMatch) {
      const id = Number(groupEditMatch[1]);
      const payload = request.postDataJSON() as { name?: string; language?: { name?: string } };
      const grp = groups.find((g) => g.id === id);
      if (grp) {
        grp.name = payload.name ?? grp.name;
        if (payload.language?.name) grp.language = { name: payload.language.name };
        state.groupEdited = true;
        return json(route, grp);
      }
      return route.fulfill({ status: 404, body: 'Group not found' });
    }

    const groupDeleteMatch = path.match(/^\/group\/(\d+)\/delete/);
    if (method === 'DELETE' && groupDeleteMatch) {
      const id = Number(groupDeleteMatch[1]);
      const idx = groups.findIndex((g) => g.id === id);
      if (idx !== -1) {
        groups.splice(idx, 1);
        state.groupDeleted = true;
      }
      return route.fulfill({ status: 204, body: '' });
    }

    // Students list (optionally by group)
    if (method === 'GET' && path === '/api/users/students') {
      const groupIdParam = url.searchParams.get('groupId');
      if (groupIdParam) {
        const gid = Number(groupIdParam);
        const filtered = students.filter((s) =>
          s.groups.some((g) => g.id === gid),
        );
        return json(route, filtered);
      }
      return json(route, students);
    }

    if (method === 'POST' && path === '/api/users/students') {
      const payload = request.postDataJSON() as {
        firstName?: string;
        lastName?: string;
        email?: string;
        password?: string;
        groupIds?: number[];
      };
      const created = {
        id: nextStudentId++,
        firstName: payload.firstName ?? 'Student',
        lastName: payload.lastName ?? 'New',
        email: payload.email ?? 'new.student@test.local',
        groups: (payload.groupIds ?? []).map((gid) => {
          const g = groups.find((x) => x.id === gid);
          return { id: gid, name: g?.name ?? `Group ${gid}` };
        }),
      };
      students.push(created);
      state.studentCreated = true;
      return json(route, created, 201);
    }

    const studentUpdateMatch = path.match(/^\/api\/users\/students\/(\d+)$/);
    if (method === 'PUT' && studentUpdateMatch) {
      const id = Number(studentUpdateMatch[1]);
      const payload = request.postDataJSON() as { firstName?: string; lastName?: string };
      const s = students.find((x) => x.id === id);
      if (s) {
        if (payload.firstName != null) s.firstName = payload.firstName;
        if (payload.lastName != null) s.lastName = payload.lastName;
        state.studentUpdated = true;
        return json(route, s);
      }
      return route.fulfill({ status: 404, body: 'Student not found' });
    }

    const studentDeleteMatch = path.match(/^\/api\/users\/students\/(\d+)$/);
    if (method === 'DELETE' && studentDeleteMatch) {
      const id = Number(studentDeleteMatch[1]);
      const idx = students.findIndex((s) => s.id === id);
      if (idx !== -1) {
        students.splice(idx, 1);
        state.studentDeleted = true;
      }
      return route.fulfill({ status: 204, body: '' });
    }

    // Teachers
    if (method === 'GET' && path === '/api/users/teachers') {
      return json(route, teachers);
    }

    if (method === 'POST' && path === '/api/users/teachers') {
      const payload = request.postDataJSON() as {
        firstName?: string;
        lastName?: string;
        email?: string;
        password?: string;
      };
      const created = {
        id: nextTeacherId++,
        firstName: payload.firstName ?? 'Teacher',
        lastName: payload.lastName ?? 'New',
        email: payload.email ?? 'new.teacher@test.local',
        groups: [] as Array<{ id: number; name: string }>,
      };
      teachers.push(created);
      state.teacherCreated = true;
      return json(route, created, 201);
    }

    const teacherUpdateMatch = path.match(/^\/api\/users\/teachers\/(\d+)$/);
    if (method === 'PUT' && teacherUpdateMatch) {
      const id = Number(teacherUpdateMatch[1]);
      const payload = request.postDataJSON() as { firstName?: string; lastName?: string };
      const t = teachers.find((x) => x.id === id);
      if (t) {
        if (payload.firstName != null) t.firstName = payload.firstName;
        if (payload.lastName != null) t.lastName = payload.lastName;
        state.teacherUpdated = true;
        return json(route, t);
      }
      return route.fulfill({ status: 404, body: 'Teacher not found' });
    }

    const teacherDeleteMatch = path.match(/^\/api\/users\/teachers\/(\d+)$/);
    if (method === 'DELETE' && teacherDeleteMatch) {
      const id = Number(teacherDeleteMatch[1]);
      const idx = teachers.findIndex((t) => t.id === id);
      if (idx !== -1) {
        teachers.splice(idx, 1);
        state.teacherDeleted = true;
      }
      return route.fulfill({ status: 204, body: '' });
    }

    // Groups by teacher for admin/teacher tabs
    const teacherMatch = path.match(/^\/group\/(\d+)\/get_groups_by_teacher$/);
    if (method === 'GET' && teacherMatch) {
      const teacherId = Number(teacherMatch[1]);
      const teacher = teachers.find((t) => t.id === teacherId);
      return json(
        route,
        teacher?.groups.map((g) => ({ id: g.id, name: g.name })) ?? [],
      );
    }

    // Add user (student/teacher) to group
    const addToGroupMatch = path.match(/^\/group\/(\d+)\/add\/(\d+)$/);
    if (method === 'POST' && addToGroupMatch) {
      const groupId = Number(addToGroupMatch[1]);
      const userId = Number(addToGroupMatch[2]);
      const group = groups.find((g) => g.id === groupId) ?? {
        id: groupId,
        name: `Group ${groupId}`,
        language: { name: 'Английский' },
      };

      const student = students.find((s) => s.id === userId);
      if (student) {
        if (!student.groups.some((g) => g.id === groupId)) {
          student.groups.push({ id: group.id, name: group.name });
        }
        state.studentAssignedToGroup.push({ groupId: group.id, userId });
        return json(route, group);
      }

      const teacher = teachers.find((t) => t.id === userId);
      if (teacher) {
        if (!teacher.groups.some((g) => g.id === groupId)) {
          teacher.groups.push({ id: group.id, name: group.name });
        }
        state.teacherAssignedToGroup.push({ groupId: group.id, userId });
        return json(route, group);
      }

      return json(route, group);
    }

    return route.fulfill({ status: 501, body: `Unhandled admin mock: ${method} ${path}` });
  });

  return state;
}

