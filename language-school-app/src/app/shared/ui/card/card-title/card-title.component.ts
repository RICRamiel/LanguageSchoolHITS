import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card-title',
  standalone: true,
  templateUrl: './card-title.component.html',
})
export class CardTitleComponent {
  readonly class = input<string>('');
}

