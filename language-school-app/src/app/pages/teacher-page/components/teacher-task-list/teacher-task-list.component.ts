import { Component, input, output } from '@angular/core';
import { TeacherTask } from '../../teacher-page.types';

@Component({
  selector: 'app-teacher-task-list',
  standalone: true,
  imports: [],
  templateUrl: './teacher-task-list.component.html',
  styleUrl: './teacher-task-list.component.less',
})
export class TeacherTaskListComponent {
  readonly tasks = input<TeacherTask[]>([]);
  readonly createTask = output<void>();
  readonly openTaskDetails = output<string>();
}
