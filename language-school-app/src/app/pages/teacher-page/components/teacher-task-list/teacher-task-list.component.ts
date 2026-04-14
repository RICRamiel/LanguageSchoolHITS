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
  readonly openGrading = output<{ taskId: string; group: string; title: string }>();

  getStatusLabel(status: TeacherTask['status']): string {
    switch (status) {
      case 'COMPLETE':
        return 'Выполнено';
      case 'OVERDUE':
        return 'Просрочено';
      case 'PENDING':
      default:
        return 'В процессе';
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
