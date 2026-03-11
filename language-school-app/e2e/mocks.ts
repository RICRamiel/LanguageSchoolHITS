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

    if (method === 'GET' && path === '/task/get_by_group_name') {
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

