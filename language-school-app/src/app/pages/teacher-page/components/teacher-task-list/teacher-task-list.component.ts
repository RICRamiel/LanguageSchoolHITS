import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { TaskDetailsOpenPayload, TeacherTask } from '../../teacher-page.types';

@Component({
  selector: 'app-teacher-task-list',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-task-list.component.html',
  styleUrl: './teacher-task-list.component.less',
})
export class TeacherTaskListComponent {
  readonly tasks = input<TeacherTask[]>([]);
  readonly createTask = output<void>();
  readonly openTaskDetails = output<TaskDetailsOpenPayload>();
}
