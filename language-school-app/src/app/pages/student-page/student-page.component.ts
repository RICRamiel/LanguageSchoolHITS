import { Component } from '@angular/core';
import {HeaderComponent} from '../../shared/ui/header/header.component';
import {TabsComponent} from '../../shared/ui/tabs/tabs.component';
import {TaskCardComponent} from '../../shared/ui/task-card/task-card.component';
import {NotificationItem, NotificationListComponent} from '../../shared/ui/notification-list/notification-list.component';

@Component({
  selector: 'app-student-page',
  standalone: true,
  imports: [
    HeaderComponent,
    TabsComponent,
    TaskCardComponent,
    NotificationListComponent,
  ],
  templateUrl: './student-page.component.html',
  styleUrl: './student-page.component.less',
})
export class StudentPageComponent {

  tabs = [
    { id: 'tasks', label: 'Задания' },
    { id: 'notifications', label: 'Уведомления', badge: 2 },
  ];

  activeTab = 'tasks';

  tasks = [
    {
      title: 'Эссе по грамматике',
      pillText: 'Сдано',
      pillVariant: 'success' as const,
      teacher: 'Смирнова Е.В.',
      description:
        'Написать эссе на тему Present Perfect. Объем: 300–500 слов. Используйте примеры из реальной жизни.',
      dueText: '20 февраля 2026',
    },
    {
      title: 'Устное задание',
      pillText: null,
      pillVariant: 'neutral' as const,
      teacher: 'Смирнова Е.В.',
      description:
        'Подготовить 3-минутный монолог о своем хобби. Запишите видео и загрузите.',
      dueText: '25 февраля 2026',
    },
  ];

  notifications: NotificationItem[] = [
    {
      id: 1,
      type: 'announcement',
      title: 'Изменение расписания',
      author: 'Смирнова Е.В.',
      dateTime: '10 февраля 2026, 10:30',
      text: 'Занятие в пятницу переносится на 16:00',
      tag: 'Объявление',
      isNew: true,
      isUnread: true,
    },
    {
      id: 2,
      type: 'task',
      title: 'Новое задание: Эссе по грамматике',
      author: 'Смирнова Е.В.',
      dateTime: '8 февраля 2026, 14:00',
      text: 'Преподаватель Смирнова Е.В. создал новое задание',
      tag: 'Задание',
      isNew: true,
      isUnread: true,
    },
    {
      id: 3,
      type: 'comment',
      title: 'Новый комментарий к вашей работе',
      author: 'Смирнова Е.В.',
      dateTime: '12 февраля 2026, 14:30',
      text: "Преподаватель оставил комментарий к заданию 'Эссе по грамматике'",
      tag: 'Комментарий',
      isNew: false,
      isUnread: false,
    },
  ];

  onTabChange(tabId: string) {
    this.activeTab = tabId;
  }

  markNotificationAsRead(notificationId: number) {
    this.notifications = this.notifications.map((item) =>
      item.id === notificationId ? { ...item, isUnread: false } : item,
    );
  }

  openTaskDetails(taskTitle: string) {
    console.log('details:', taskTitle);
  }



  protected onLogout = () =>{
    console.log('logout');
  };
}
