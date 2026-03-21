import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-card-header',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './card-header.component.html',
})
export class CardHeaderComponent {
  readonly class = input<string>('');
}

