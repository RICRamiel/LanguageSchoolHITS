import { Component, output } from '@angular/core';
import { CreateTaskPayload } from '../../teacher-page.types';

@Component({
  selector: 'app-create-task-modal',
  standalone: true,
  imports: [],
  templateUrl: './create-task-modal.component.html',
  styleUrl: './create-task-modal.component.less',
})
export class CreateTaskModalComponent {
  title = '';
  description = '';
  dueDate = '';
  groupId = '1';

  readonly close = output<void>();
  readonly submit = output<CreateTaskPayload>();

  onSubmit() {
    this.submit.emit({
      title: this.title,
      description: this.description,
      dueDate: this.dueDate,
      groupId: this.groupId,
    });
  }
}
