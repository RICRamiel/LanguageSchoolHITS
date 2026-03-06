import { Component, output } from '@angular/core';
import { CreateNotificationPayload } from '../../teacher-page.types';

@Component({
  selector: 'app-create-notification-modal',
  standalone: true,
  imports: [],
  templateUrl: './create-notification-modal.component.html',
  styleUrl: './create-notification-modal.component.less',
})
export class CreateNotificationModalComponent {
  title = '';
  content = '';
  groupId = '1';

  readonly close = output<void>();
  readonly submit = output<CreateNotificationPayload>();

  onSubmit() {
    this.submit.emit({
      title: this.title,
      content: this.content,
      groupId: this.groupId,
    });
  }
}
