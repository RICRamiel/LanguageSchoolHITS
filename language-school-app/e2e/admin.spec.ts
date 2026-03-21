import { expect, test } from '@playwright/test';
import { mockAdminApi } from './mocks';

test.describe('Admin core flows', () => {
  test('manage languages, groups, students and teachers', async ({ page }) => {
    test.setTimeout(90_000);
    const state = await mockAdminApi(page);

    await page.goto('/admin');

    // Заголовок и табы
    await expect(page.getByText('Панель администратора')).toBeVisible();
    const tabs = page.locator('.tabs .tab');
    await expect(tabs).toHaveCount(4);

    // Вкладка «Языки»: добавить язык
    await tabs.filter({ hasText: 'Языки' }).click();
    await page.getByRole('button', { name: 'Добавить язык' }).click();
    await expect(page.locator('app-language-form')).toBeVisible();

    await page.locator('app-language-form input[placeholder="Название языка"]').fill('Испанский');
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.languageCreated).toBe(true);

    // Редактирование языка: Изменить «Испанский» → «Испанский (ред.)»
    await page.getByRole('row').filter({ hasText: 'Испанский' }).getByRole('button', { name: 'Изменить' }).click();
    await expect(page.locator('app-language-form')).toBeVisible();
    await page.locator('app-language-form input[placeholder="Название языка"]').fill('Испанский (ред.)');
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.languageEdited).toBe(true);

    // Удаление языка
    await page.getByRole('row').filter({ hasText: 'Испанский (ред.)' }).getByRole('button', { name: 'Удалить' }).click();
    await expect.poll(() => state.languageDeleted).toBe(true);

    // Вкладка «Группы»: добавить группу
    await tabs.filter({ hasText: 'Группы' }).click();
    await page.getByRole('button', { name: 'Добавить группу' }).click();
    await expect(page.locator('app-group-form')).toBeVisible();

    await page.locator('app-group-form input[placeholder="Название группы"]').fill('B1');
    await page.locator('#group-language').selectOption({ label: 'Английский' });
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.groupCreated).toBe(true);

    // Редактирование группы: B1 → B1-ред
    await page.getByRole('row').filter({ hasText: 'B1' }).getByRole('button', { name: 'Изменить' }).click();
    await expect(page.locator('app-group-form')).toBeVisible();
    await page.locator('app-group-form input[placeholder="Название группы"]').fill('B1-ред');
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.groupEdited).toBe(true);

    // Удаление группы
    await page.getByRole('row').filter({ hasText: 'B1-ред' }).getByRole('button', { name: 'Удалить' }).click();
    await expect.poll(() => state.groupDeleted).toBe(true);

    // Вкладка «Студенты»: добавить студента, привязать к группе, редактировать, удалить
    await tabs.filter({ hasText: 'Студенты' }).click();
    await page.getByRole('button', { name: 'Добавить студента' }).click();
    await expect(page.locator('app-student-form')).toBeVisible();

    await page.locator('app-student-form input[placeholder="Имя"]').fill('Иван');
    await page.locator('app-student-form input[placeholder="Фамилия"]').fill('Иванов');
    await page.locator('app-student-form input[placeholder="email@example.com"]').fill('ivan@student.test');
    await page.locator('app-student-form input[type="password"]').fill('password123');
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.studentCreated).toBe(true);

    // Привязка студента к группе (группа A1 id=10)
    await page.getByRole('row').filter({ hasText: 'Иван Иванов' }).getByRole('button', { name: 'Привязать к группе' }).click();
    await expect(page.locator('app-assign-group-modal .assign-modal')).toBeVisible();
    const assignSelect = page.locator('app-assign-group-modal #assign-group');
    await assignSelect.click();
    await page.keyboard.press('ArrowDown'); // «Выберите группу» → «A1 (Английский)»
    await page.keyboard.press('Enter');
    const saveBtn = page.locator('app-assign-group-modal').getByRole('button', { name: 'Сохранить' });
    await expect(saveBtn).toBeEnabled({ timeout: 5000 });
    await saveBtn.click();
    await expect.poll(() => state.studentAssignedToGroup.length > 0).toBe(true);

    // Редактирование студента: Иван Иванов → Иван Петров
    await page.getByRole('row').filter({ hasText: 'Иван Иванов' }).getByRole('button', { name: 'Изменить' }).click();
    await expect(page.locator('app-student-form')).toBeVisible();
    await page.locator('app-student-form input[placeholder="Фамилия"]').fill('Петров');
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.studentUpdated).toBe(true);

    // Удаление студента (ищем по email, чтобы не зависеть от перерисовки)
    await page.getByRole('row').filter({ hasText: 'ivan@student.test' }).getByRole('button', { name: 'Удалить' }).click();
    await expect.poll(() => state.studentDeleted).toBe(true);

    // Вкладка «Преподаватели»: добавить преподавателя, редактировать, удалить
    await tabs.filter({ hasText: 'Преподаватели' }).click();
    await page.getByRole('button', { name: 'Добавить преподавателя' }).click();
    await expect(page.locator('app-teacher-form')).toBeVisible();

    await page.locator('app-teacher-form input[placeholder="Имя"]').fill('Мария');
    await page.locator('app-teacher-form input[placeholder="Фамилия"]').fill('Петрова');
    await page.locator('app-teacher-form input[placeholder="email@school.com"]').fill('maria@teacher.test');
    await page.locator('app-teacher-form input[type="password"]').fill('password123');
    await page.locator('#teacher-group').selectOption({ label: 'A1' });
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.teacherCreated).toBe(true);

    // Редактирование преподавателя: Мария Петрова → Мария Сидорова
    await page.getByRole('row').filter({ hasText: 'Мария Петрова' }).getByRole('button', { name: 'Изменить' }).click();
    await expect(page.locator('app-teacher-form')).toBeVisible();
    await page.locator('app-teacher-form input[placeholder="Фамилия"]').fill('Сидорова');
    await page.getByRole('button', { name: 'Сохранить' }).click();
    await expect.poll(() => state.teacherUpdated).toBe(true);

    // Удаление преподавателя (ищем по email)
    await page.getByRole('row').filter({ hasText: 'maria@teacher.test' }).getByRole('button', { name: 'Удалить' }).click();
    await expect.poll(() => state.teacherDeleted).toBe(true);
  });
});

