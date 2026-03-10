import { Component, input, output } from '@angular/core';
import { StudentTask } from '../../student-page.types';

@Component({
  selector: 'app-student-task-details-modal',
  standalone: true,
  imports: [],
  templateUrl: './student-task-details-modal.component.html',
  styleUrl: './student-task-details-modal.component.less',
})
export class StudentTaskDetailsModalComponent {
  readonly task = input.required<StudentTask>();
  readonly close = output<void>();
}
