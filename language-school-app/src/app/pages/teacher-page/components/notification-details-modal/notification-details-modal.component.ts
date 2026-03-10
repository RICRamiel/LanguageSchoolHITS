import { Component, input, output } from '@angular/core';
import { TeacherNotification } from '../../teacher-page.types';

@Component({
  selector: 'app-notification-details-modal',
  standalone: true,
  imports: [],
  templateUrl: './notification-details-modal.component.html',
  styleUrl: './notification-details-modal.component.less',
})
export class NotificationDetailsModalComponent {
  readonly notification = input.required<TeacherNotification>();
  readonly close = output<void>();
}
