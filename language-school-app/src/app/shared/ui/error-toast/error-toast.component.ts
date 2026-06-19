import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ErrorToastService } from '../../../core/errors/error-toast.service';

@Component({
  selector: 'app-error-toast',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './error-toast.component.html',
  styleUrl: './error-toast.component.less',
})
export class ErrorToastComponent {
  protected readonly errorToasts = inject(ErrorToastService);
}
