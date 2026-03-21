import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-card-title',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './card-title.component.html',
})
export class CardTitleComponent {
  readonly class = input<string>('');
}

