import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { catchError, map, mergeMap, Observable, of, tap, throwError } from 'rxjs';
import { LoginRequest, LoginResponse, LoginResult } from './auth.models';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';
import { UserMeResponse } from '../user/user.models';
import { UserService } from '../user/user.service';

const TOKEN_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly userService = inject(UserService);

  login(payload: LoginRequest): Observable<LoginResult> {
    return this.http
      .post(withOpenApiBase(OPENAPI_PATHS.auth.login), payload, {
        responseType: 'text',
      })
      .pipe(
        map((body) => this.extractToken(body)),
        mergeMap((token) => {
          if (!token) {
            return throwError(() => new Error('Login token is missing in response.'));
          }
          return of(token);
        }),
        tap((token) => {
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem(TOKEN_KEY, token);
          }
        }),
        mergeMap((token) =>
          this.http.get<UserMeResponse>(withOpenApiBase(OPENAPI_PATHS.users.me)).pipe(
            map((me) => {
              this.userService.setCachedMe(me);
              return {
                token,
                redirectPath: this.resolveRedirectPath(me.role),
              };
            }),
            catchError(() =>
              of({
                token,
                redirectPath: '/student',
              }),
            ),
          ),
        ),
      );
  }

  logout(): Observable<void> {
    return this.http
      .post(withOpenApiBase(OPENAPI_PATHS.auth.logout), {}, { responseType: 'text' })
      .pipe(
        tap(() => {
          this.userService.clearCachedMe();
          if (isPlatformBrowser(this.platformId)) {
            localStorage.removeItem(TOKEN_KEY);
          }
        }),
        map(() => void 0),
      );
  }

  getToken(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem(TOKEN_KEY) : null;
  }

  private extractToken(body: string | null): string | null {
    const raw = body?.trim();
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as LoginResponse | string;
      if (typeof parsed === 'string') {
        return parsed.trim() || null;
      }
      return parsed?.token?.trim() || null;
    } catch {
      const unquoted = raw.replace(/^"+|"+$/g, '').trim();
      return unquoted || null;
    }
  }

  private resolveRedirectPath(role: UserMeResponse['role']): string {
    switch (role) {
      case 'ADMIN':
        return '/admin';
      case 'TEACHER':
        return '/teacher';
      case 'STUDENT':
      default:
        return '/student';
    }
  }
}
