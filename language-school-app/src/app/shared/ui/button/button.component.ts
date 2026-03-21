import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-button',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './button.component.html',
  styleUrl: './button.component.less',
})
export class ButtonComponent {
  readonly type = input<'button' | 'submit' | 'reset'>('button');
  readonly class = input<string>('');
  readonly disabled = input<boolean>(false);
}

