import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type NotificationType = 'announcement' | 'task' | 'comment';

export type NotificationItem = {
  id: string;
  type: NotificationType;
  title: string;
  author: string;
  dateTime: string;
  text: string;
  tag: string;
};

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './notification-list.component.html',
  styleUrl: './notification-list.component.less',
})
export class NotificationListComponent {
  readonly notifications = input<NotificationItem[]>([]);
}
