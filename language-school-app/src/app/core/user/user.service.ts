import { HttpClient } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, of, tap } from 'rxjs';
import { UserMeResponse } from './user.models';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';

const USER_ME_CACHE_KEY = 'user_me_cache';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private cachedMe: UserMeResponse | null = this.readCachedMeFromStorage();

  getMe(forceRefresh = false): Observable<UserMeResponse> {
    if (!forceRefresh && this.cachedMe) {
      return of(this.cachedMe);
    }

    return this.http.get<UserMeResponse>(withOpenApiBase(OPENAPI_PATHS.users.me)).pipe(
      tap((me) => {
        this.setCachedMe(me);
      }),
    );
  }

  setCachedMe(me: UserMeResponse): void {
    this.cachedMe = me;
    this.writeCachedMeToStorage(me);
  }

  clearCachedMe(): void {
    this.cachedMe = null;
    this.clearCachedMeFromStorage();
  }

  private readCachedMeFromStorage(): UserMeResponse | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const raw = localStorage.getItem(USER_ME_CACHE_KEY);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as UserMeResponse;
    } catch {
      localStorage.removeItem(USER_ME_CACHE_KEY);
      return null;
    }
  }

  private writeCachedMeToStorage(me: UserMeResponse): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    localStorage.setItem(USER_ME_CACHE_KEY, JSON.stringify(me));
  }

  private clearCachedMeFromStorage(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    localStorage.removeItem(USER_ME_CACHE_KEY);
  }
}
