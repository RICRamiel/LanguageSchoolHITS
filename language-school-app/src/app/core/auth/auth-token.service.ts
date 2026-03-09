import { Injectable, signal } from '@angular/core';

const TOKEN_KEY = 'auth_token';

/**
 * Хранит JWT для API. Устанавливается после логина (AuthControllerService.login),
 * очищается при логауте. Используется интерцептором для заголовка Authorization.
 * Сохраняет токен в localStorage для сохранения при перезагрузке и ручного тестирования.
 */
@Injectable({ providedIn: 'root' })
export class AuthTokenService {
  private readonly tokenSignal = signal<string | null>(
    typeof localStorage !== 'undefined' ? localStorage.getItem(TOKEN_KEY) : null
  );

  readonly token = this.tokenSignal.asReadonly();

  setToken(value: string | null): void {
    this.tokenSignal.set(value);
    if (typeof localStorage !== 'undefined') {
      if (value) {
        localStorage.setItem(TOKEN_KEY, value);
      } else {
        localStorage.removeItem(TOKEN_KEY);
      }
    }
  }
}
