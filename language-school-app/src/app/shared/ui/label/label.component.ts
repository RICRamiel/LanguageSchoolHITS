import { Component, input } from '@angular/core';

@Component({
  selector: 'app-label',
  standalone: true,
  templateUrl: './label.component.html',
  styleUrl: './label.component.less',
})
export class LabelComponent {
  readonly for = input<string>('');
  readonly class = input<string>('');
}

