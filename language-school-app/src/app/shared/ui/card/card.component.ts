import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './card.component.html',
})
export class CardComponent {
  readonly class = input<string>('');
}

