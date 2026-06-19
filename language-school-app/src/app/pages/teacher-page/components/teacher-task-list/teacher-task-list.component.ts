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

  getTeamSizeLabel(task: TeacherTask): string {
    return `В команде: ${this.formatRange(task.minTeamSize, task.maxTeamSize)}`;
  }

  getTeamsAmountLabel(task: TeacherTask): string {
    const limit = this.formatRange(task.minTeamsAmount, task.maxTeamsAmount);
    return task.teams.length > 0 ? `Команд: ${task.teams.length} / ${limit}` : `Команд: ${limit}`;
  }

  private formatRange(min: number | null, max: number | null): string {
    if (min !== null && max !== null) {
      return min === max ? String(min) : `${min}-${max}`;
    }
    if (min !== null) {
      return `от ${min}`;
    }
    if (max !== null) {
      return `до ${max}`;
    }
    return 'не задано';
  }
}
