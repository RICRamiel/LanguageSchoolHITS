import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card-description',
  standalone: true,
  templateUrl: './card-description.component.html',
})
export class CardDescriptionComponent {
  readonly class = input<string>('');
}

