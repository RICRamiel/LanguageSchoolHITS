import { expect, test } from '@playwright/test';
import { mockTeacherApi } from './mocks';

test('Teacher: create notification, create task, leave comment', async ({ page }) => {
  const state = await mockTeacherApi(page);

  await page.goto('/teacher');

  await page.locator('.tabs .tab').nth(1).click();
  await page.locator('.notifications-section__create').click();
  await expect(page.locator('app-create-notification-modal .modal')).toBeVisible();
  await page.fill('#notify-title', 'Новость для группы');
  await page.fill('#notify-content', 'Сегодня перенос занятия');
  await page.locator('app-create-notification-modal .modal__btn--primary').click();
  await expect.poll(() => state.notificationCreated).toBe(true);

  await page.locator('.tabs .tab').nth(0).click();
  await page.locator('.tasks-section__create').click();
  await expect(page.locator('app-create-task-modal .modal')).toBeVisible();
  await page.fill('#task-title', 'Домашнее задание');
  await page.fill('#task-content', 'Выполнить упражнения 1-5');
  await page.fill('#task-date', '2026-03-20');
  await page.locator('app-create-task-modal .modal__btn--primary').click();
  await expect.poll(() => state.taskCreated).toBe(true);

  const firstTaskCard = page.locator('.task-card').first();
  await expect(firstTaskCard).toBeVisible();
  await firstTaskCard.locator('.meta-item--action').nth(1).click();

  const commentInput = page.locator('app-task-details-modal .modal__comment-input');
  await expect(commentInput).toBeVisible();
  await commentInput.fill('Комментарий от учителя');
  await page.locator('app-task-details-modal .modal__comment-submit').click();

  await expect.poll(() => state.teacherCommentText).toBe('Комментарий от учителя');
});

test('Scenario: преподаватель видит результаты peer-оценивания', async ({ page }) => {
  await test.step('Given команды отправили peer-оценки', async () => {
    await mockTeacherApi(page, { peerReviewResults: true });
    await page.goto('/teacher');
    await expect(page.getByText('Peer-проект')).toBeVisible();
  });

  await test.step('When преподаватель открывает результаты задания', async () => {
    const taskCard = page.locator('.task-card').filter({ hasText: 'Peer-проект' });
    await taskCard.getByRole('button', { name: /Подробнее/ }).click();
    await page.getByRole('button', { name: 'Peer-оценки' }).click();
  });

  await test.step('Then он видит оцениваемую команду', async () => {
    await expect(page.getByText('Оцениваемая команда: Alpha')).toBeVisible();
  });

  await test.step('And видит команду-оценщика', async () => {
    await expect(page.getByText('Команда-оценщик: Beta')).toBeVisible();
  });

  await test.step('And видит оценки по критериям', async () => {
    await expect(page.getByText('Содержание')).toBeVisible();
    await expect(page.getByText('4 / 5')).toBeVisible();
    await expect(page.getByText('Язык', { exact: true })).toBeVisible();
    await expect(page.getByText('5 / 5')).toBeVisible();
  });

  await test.step('And видит комментарии', async () => {
    await expect(page.getByText('Аргументация сильная')).toBeVisible();
    await expect(page.getByText('Комментарий по языку')).toBeVisible();
  });
});
