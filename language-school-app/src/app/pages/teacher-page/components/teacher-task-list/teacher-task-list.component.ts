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
  readonly openGrading = output<{ taskId: number; group: string; title: string }>();

  getStatusLabel(status: TeacherTask['status']): string {
    switch (status) {
      case 'COMPLETE':
        return 'Complete';
      case 'OVERDUE':
        return 'Overdue';
      case 'PENDING':
      default:
        return 'In Progress';
    }
  }

  getStatusClass(status: TeacherTask['status']): string {
    switch (status) {
      case 'COMPLETE':
        return 'task-card__status--complete';
      case 'OVERDUE':
        return 'task-card__status--overdue';
      case 'PENDING':
      default:
        return 'task-card__status--pending';
    }
  }
}
