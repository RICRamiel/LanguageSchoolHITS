import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { StudentNotification } from '../../student-page.types';

@Component({
  selector: 'app-student-notification-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './student-notification-modal.component.html',
  styleUrl: './student-notification-modal.component.less',
})
export class StudentNotificationModalComponent {
  readonly notification = input.required<StudentNotification>();
  readonly close = output<void>();
}
