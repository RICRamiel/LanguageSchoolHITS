import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../shared/ui/header/header.component';
import { TabsComponent } from '../../shared/ui/tabs/tabs.component';
import {
  CreateNotificationPayload,
  CreateTaskPayload,
  TaskDetailsOpenPayload,
  TeacherTaskDetailsSection,
  TeacherNotification,
  TeacherTask,
} from './teacher-page.types';
import { TeacherTaskListComponent } from './components/teacher-task-list/teacher-task-list.component';
import { TeacherNotificationsComponent } from './components/teacher-notifications/teacher-notifications.component';
import { CreateNotificationModalComponent } from './components/create-notification-modal/create-notification-modal.component';
import { CreateTaskModalComponent } from './components/create-task-modal/create-task-modal.component';
import { TaskDetailsModalComponent } from './components/task-details-modal/task-details-modal.component';
import { NotificationDetailsModalComponent } from './components/notification-details-modal/notification-details-modal.component';
import { AuthService } from '../../core/auth/auth.service';

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
    TaskDetailsModalComponent,
    NotificationDetailsModalComponent,
  ],
  templateUrl: './teacher-page.component.html',
  styleUrl: './teacher-page.component.less',
})
export class TeacherPageComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  tabs = [
    { id: 'tasks', label: 'Задания' },
    { id: 'notifications', label: 'Уведомления', badge: 3 },
  ];

  activeTab = 'tasks';
  isTaskModalOpen = false;
  isNotificationModalOpen = false;
  isTaskDetailsModalOpen = false;
  isNotificationDetailsModalOpen = false;
  selectedTask: TeacherTask | null = null;
  selectedTaskSection: TeacherTaskDetailsSection = 'overview';
  selectedNotification: TeacherNotification | null = null;

  tasks: TeacherTask[] = [
    {
      title: 'Эссе по грамматике',
      description: 'Написать эссе на тему Present Perfect',
      dueDate: '20 февраля 2026',
      submissions: '12 работ',
      comments: '5 комментариев',
      group: 'Группа А-101',
      attachedWorks: [
        {
          studentName: 'Иван Петров',
          fileName: 'essay-present-perfect-petrov.docx',
          submittedAt: '18 февраля 2026, 16:20',
          content: 'Эссе Ивана Петрова по теме Present Perfect.',
        },
        {
          studentName: 'Мария Сидорова',
          fileName: 'present-perfect-essay-sidorova.pdf',
          submittedAt: '19 февраля 2026, 11:05',
          content: 'Эссе Марии Сидоровой по теме Present Perfect.',
        },
      ],
      taskComments: [
        {
          studentName: 'Иван Петров',
          text: 'Можно ли использовать примеры из интервью?',
          createdAt: '18 февраля 2026, 18:03',
        },
        {
          studentName: 'Мария Сидорова',
          text: 'Нужно ли оформлять список источников?',
          createdAt: '19 февраля 2026, 12:44',
        },
      ],
    },
    {
      title: 'Устное задание',
      description: 'Подготовить 3-минутный монолог о своем хобби',
      dueDate: '25 февраля 2026',
      submissions: '8 работ',
      comments: '3 комментариев',
      group: 'Группа Б-202',
      attachedWorks: [
        {
          studentName: 'Анна Волкова',
          fileName: 'hobby-monologue-volkova.mp4',
          submittedAt: '23 февраля 2026, 20:15',
          content: 'Ссылка на видео-монолог Анны Волковой о хобби.',
        },
      ],
      taskComments: [
        {
          studentName: 'Анна Волкова',
          text: 'Можно ли сдавать аудио вместо видео?',
          createdAt: '23 февраля 2026, 19:01',
        },
      ],
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
    this.tasks = [
      {
        title: payload.title,
        description: payload.description,
        dueDate: this.formatIsoDate(payload.dueDate),
        submissions: '0 работ',
        comments: '0 комментариев',
        group: `Группа ${payload.groupId}`,
        attachedWorks: [],
        taskComments: [],
      },
      ...this.tasks,
    ];
    this.closeCreateTaskModal();
  }

  openCreateNotificationModal() {
    this.isNotificationModalOpen = true;
  }

  closeCreateNotificationModal() {
    this.isNotificationModalOpen = false;
  }

  submitNotification(payload: CreateNotificationPayload) {
    const nextId = Math.max(0, ...this.notifications.map((item) => item.id)) + 1;
    this.notifications = [
      {
        id: nextId,
        title: payload.title,
        text: payload.content,
        date: this.formatToday(),
      },
      ...this.notifications,
    ];
    this.closeCreateNotificationModal();
  }

  onOpenTaskDetails(payload: TaskDetailsOpenPayload) {
    const task = this.tasks.find((item) => item.title === payload.title);
    if (!task) {
      return;
    }

    this.selectedTask = task;
    this.selectedTaskSection = payload.section;
    this.isTaskDetailsModalOpen = true;
  }

  onOpenNotification(notificationId: number) {
    const notification = this.notifications.find((item) => item.id === notificationId);
    if (!notification) {
      return;
    }

    this.selectedNotification = notification;
    this.isNotificationDetailsModalOpen = true;
  }

  closeTaskDetailsModal() {
    this.selectedTask = null;
    this.selectedTaskSection = 'overview';
    this.isTaskDetailsModalOpen = false;
  }

  closeNotificationDetailsModal() {
    this.selectedNotification = null;
    this.isNotificationDetailsModalOpen = false;
  }

  onLogout() {
    this.authService.logout().subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
      },
    });
  }

  private formatIsoDate(isoDate: string): string {
    if (!isoDate) {
      return '';
    }

    const date = new Date(isoDate);
    if (Number.isNaN(date.getTime())) {
      return isoDate;
    }

    return new Intl.DateTimeFormat('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(date);
  }

  private formatToday(): string {
    return new Intl.DateTimeFormat('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date());
  }
}
