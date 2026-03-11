import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { TeacherNotification } from '../../teacher-page.types';

@Component({
  selector: 'app-teacher-notifications',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-notifications.component.html',
  styleUrl: './teacher-notifications.component.less',
})
export class TeacherNotificationsComponent {
  readonly notifications = input<TeacherNotification[]>([]);
  readonly createNotification = output<void>();
  readonly openNotification = output<string>();
}
