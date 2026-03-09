import { Component } from '@angular/core';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  TeacherNotification,
  TeacherTask,
} from './teacher-page.types';
import { TeacherTaskListComponent } from './components/teacher-task-list/teacher-task-list.component';
import { TeacherNotificationsComponent } from './components/teacher-notifications/teacher-notifications.component';
import { CreateNotificationModalComponent } from './components/create-notification-modal/create-notification-modal.component';
import { CreateTaskModalComponent } from './components/create-task-modal/create-task-modal.component';

@Component({
  selector: 'app-teacher-page',
  standalone: true,
  imports: [
    HeaderComponent,
    TabsComponent,
    TeacherTaskListComponent,
    TeacherNotificationsComponent,
    CreateNotificationModalComponent,
    CreateTaskModalComponent,
  ],
  templateUrl: './teacher-page.component.html',
  styleUrl: './teacher-page.component.less',
})
export class TeacherPageComponent {
  tabs = [
    { id: 'tasks', label: 'Задания' },
    { id: 'notifications', label: 'Уведомления', badge: 3 },
  ];

  activeTab = 'tasks';
  isTaskModalOpen = false;
  isNotificationModalOpen = false;

  tasks: TeacherTask[] = [
    {
      title: 'Эссе по грамматике',
      description: 'Написать эссе на тему Present Perfect',
      dueDate: '20 февраля 2026',
      submissions: '12 работ',
      comments: '5 комментариев',
      group: 'Группа А-101',
    },
    {
      title: 'Устное задание',
      description: 'Подготовить 3-минутный монолог о своем хобби',
      dueDate: '25 февраля 2026',
      submissions: '8 работ',
      comments: '3 комментариев',
      group: 'Группа Б-202',
    },
  ];

  notifications: TeacherNotification[] = [
    {
      id: 1,
      title: 'Новая работа от группы А-101',
      text: 'Поступила новая работа по заданию "Эссе по грамматике".',
      date: '1 марта 2026',
    },
    {
      id: 2,
      title: 'Комментарий к заданию',
      text: 'Студент оставил уточняющий вопрос к устному заданию.',
      date: '2 марта 2026',
    },
    {
      id: 3,
      title: 'Напоминание о дедлайне',
      text: 'Через 2 дня заканчивается срок сдачи задания.',
      date: '3 марта 2026',
    },
  ];

  onTabChange(tabId: string) {
    this.activeTab = tabId;
  }

  onCreateTask() {
    this.isTaskModalOpen = true;
  }

  closeCreateTaskModal() {
    this.isTaskModalOpen = false;
  }

  submitTask(payload: CreateTaskPayload) {
    console.log('submit task:', payload);
    this.closeCreateTaskModal();
  }

  openCreateNotificationModal() {
    this.isNotificationModalOpen = true;
  }

  closeCreateNotificationModal() {
    this.isNotificationModalOpen = false;
  }

  submitNotification(payload: CreateNotificationPayload) {
    console.log('submit notification:', payload);
    this.closeCreateNotificationModal();
  }

  onOpenTaskDetails(taskTitle: string) {
    console.log('open task details modal:', taskTitle);
  }

  onOpenNotification(notificationId: number) {
    console.log('open notification modal:', notificationId);
  }

  onLogout() {
    console.log('logout');
  }
}
