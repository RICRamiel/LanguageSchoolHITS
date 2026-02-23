import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card-header',
  standalone: true,
  templateUrl: './card-header.component.html',
})
export class CardHeaderComponent {
  readonly class = input<string>('');
}

