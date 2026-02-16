import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { LabelComponent } from '../../shared/ui/label/label.component';
import { LoginCardComponent } from '../../features/login/login-card.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonComponent,
    LabelComponent,
    LoginCardComponent,
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.less',
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  onLogin(event: Event): void {
    event.preventDefault();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { email } = this.form.getRawValue();
    if (email === 'admin@example.com') {
      this.router.navigate(['/admin']);
    } else if (email === 'teacher@example.com') {
      this.router.navigate(['/teacher']);
    } else if (email === 'student@example.com') {
      this.router.navigate(['/student']);
    }
  }
}
