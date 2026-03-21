import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-label',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './label.component.html',
  styleUrl: './label.component.less',
})
export class LabelComponent {
  readonly for = input<string>('');
  readonly class = input<string>('');
}

