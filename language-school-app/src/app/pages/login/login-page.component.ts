import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { LabelComponent } from '../../shared/ui/label/label.component';
import { LoginCardComponent } from '../../features/login/login-card.component';
import { InputComponent } from '../../shared/ui/input/input.component';
import { AuthControllerService } from '../../api/api/authController.service';
import { AuthTokenService } from '../../core/auth/auth-token.service';
import { catchError, EMPTY, finalize } from 'rxjs';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonComponent,
    LabelComponent,
    LoginCardComponent,
    InputComponent,
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.less',
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authController = inject(AuthControllerService);
  private readonly authToken = inject(AuthTokenService);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  onLogin(event: Event): void {
    event.preventDefault();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const { email, password } = this.form.getRawValue();
    this.authController.login({ email, password })
      .pipe(
        finalize(() => this.loading.set(false)),
        catchError(err => {
          this.error.set(err?.error?.message ?? err?.message ?? 'Неверный email или пароль');
          return EMPTY;
        })
      )
      .subscribe({
        next: res => {
          this.authToken.setToken(res.token);
          this.router.navigateByUrl('/admin');
        },
      });
  }
}
