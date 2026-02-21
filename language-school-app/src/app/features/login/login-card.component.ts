import { Component } from '@angular/core';
import { CardComponent } from '../../shared/ui/card/card.component';
import { CardHeaderComponent } from '../../shared/ui/card/card-header/card-header.component';
import { CardTitleComponent } from '../../shared/ui/card/card-title/card-title.component';
import { CardContentComponent } from '../../shared/ui/card/card-content/card-content.component';

@Component({
  selector: 'app-login-card',
  standalone: true,
  imports: [
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
  ],
  templateUrl: './login-card.component.html',
  styleUrl: './login-card.component.less',
})
export class LoginCardComponent {}

