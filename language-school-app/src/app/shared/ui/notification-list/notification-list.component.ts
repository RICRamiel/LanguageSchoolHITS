import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NotificationAttachment } from '../../../core/teacher/teacher.models';

export type NotificationType = 'announcement' | 'task' | 'comment';

export type NotificationItem = {
  id: string;
  type: NotificationType;
  title: string;
  author: string;
  dateTime: string;
  text: string;
  tag: string;
  attachment: NotificationAttachment | null;
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
  readonly downloadAttachment = output<NotificationAttachment>();
}
