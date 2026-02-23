import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card-content',
  standalone: true,
  templateUrl: './card-content.component.html',
})
export class CardContentComponent {
  readonly class = input<string>('');
}

