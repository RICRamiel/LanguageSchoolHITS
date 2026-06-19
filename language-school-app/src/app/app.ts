import { Component, signal, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ErrorToastComponent } from './shared/ui/error-toast/error-toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ErrorToastComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.html',
  styleUrl: './app.less'
})
export class App {
  protected readonly title = signal('language-school-app');
}
