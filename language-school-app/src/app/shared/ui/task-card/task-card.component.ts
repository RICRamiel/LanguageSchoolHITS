import { Component, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';

type PillVariant = 'success' | 'neutral';

@Component({
  selector: 'app-task-card',
  standalone: true,
  imports: [ButtonComponent],
  templateUrl: './task-card.component.html',
  styleUrl: './task-card.component.less',
})
export class TaskCardComponent {
  readonly title = input<string>('');
  readonly teacher = input<string>('');
  readonly description = input<string>('');
  readonly dueText = input<string>('');

  readonly pillText = input<string | null>(null);
  readonly pillVariant = input<PillVariant>('neutral');

  readonly details = output<void>();
}
