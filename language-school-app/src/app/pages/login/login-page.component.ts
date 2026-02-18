import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { LabelComponent } from '../../shared/ui/label/label.component';
import { LoginCardComponent } from '../../features/login/login-card.component';
import {InputComponent} from '../../shared/ui/input/input.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonComponent,
    LabelComponent,
    LoginCardComponent,
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
  }
}
