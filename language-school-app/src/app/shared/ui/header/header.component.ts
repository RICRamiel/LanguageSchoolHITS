import { Component, computed, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [ButtonComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.less',
})
export class HeaderComponent {
  readonly fullName = input<string>('');
  readonly email = input<string>('');
  readonly badge = input<string>('');
  readonly photoUrl = input<string | null>(null);

  readonly logout = output<void>();

  readonly initials = computed(() => {
    const parts = (this.fullName() || '').trim().split(/\s+/).filter(Boolean);
    return parts.slice(0, 3).map(p => p[0]?.toUpperCase() ?? '').join('');
  });
}
