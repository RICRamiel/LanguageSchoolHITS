import { Component, computed, input, output } from '@angular/core';

export type NotificationType = 'announcement' | 'task' | 'comment';

export type NotificationItem = {
  id: number;
  type: NotificationType;
  title: string;
  author: string;
  dateTime: string;
  text: string;
  tag: string;
  isNew: boolean;
  isUnread: boolean;
};

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [],
  templateUrl: './notification-list.component.html',
  styleUrl: './notification-list.component.less',
})
export class NotificationListComponent {
  readonly notifications = input<NotificationItem[]>([]);
  readonly notificationRead = output<number>();

  readonly unreadCount = computed(
    () => this.notifications().filter((item) => item.isUnread).length,
  );

  markAsRead(notificationId: number) {
    this.notificationRead.emit(notificationId);
  }
}
