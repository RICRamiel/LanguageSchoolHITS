import { Injectable, signal } from '@angular/core';

/**
 * Хранит JWT для API. Устанавливается после логина (AuthControllerService.login),
 * очищается при логауте. Используется интерцептором для заголовка Authorization.
 */
@Injectable({ providedIn: 'root' })
export class AuthTokenService {
  private readonly tokenSignal = signal<string | null>(null);

  readonly token = this.tokenSignal.asReadonly();

  setToken(value: string | null): void {
    this.tokenSignal.set(value);
  }
}
