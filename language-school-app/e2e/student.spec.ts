import { expect, test } from '@playwright/test';
import { mockStudentApi } from './mocks';

test.describe('Student core flows', () => {
  test('open task, attach file, complete task', async ({ page }) => {
    const state = await mockStudentApi(page);

    await page.goto('/student');
    await expect(page.locator('app-task-card')).toHaveCount(1);

    await page.locator('app-task-card .bottom button').first().click();
    await expect(page.locator('.modal__title')).toBeVisible();

    const fileInput = page.locator('.modal__file-label input[type="file"]');
    await fileInput.setInputFiles({
      name: 'solution.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('student solution'),
    });

    await page.locator('.modal__action-btn').first().click();
    await expect(page.locator('.modal__download-link')).toBeVisible();

    await page.locator('.modal__action-btn--primary').click();
    await expect.poll(() => state.uploadedTaskIds.includes(2)).toBe(true);
    await expect.poll(() => state.completedTaskIds.includes(2)).toBe(true);
  });

  test('open task and leave comment', async ({ page }) => {
    const state = await mockStudentApi(page);

    await page.goto('/student');
    await page.locator('app-task-card .bottom button').first().click();

    const commentInput = page.locator('.modal__comment-input');
    await commentInput.fill('Комментарий студента');
    await page.locator('.modal__comments .modal__action-btn').last().click();

    await expect(page.locator('.modal__comments-list .modal__comment-text')).toContainText(
      'Комментарий студента',
    );
    expect(state.commentText).toBe('Комментарий студента');
  });
});
