import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, of, tap } from 'rxjs';
import { UserMeResponse } from './user.models';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private cachedMe: UserMeResponse | null = null;

  getMe(forceRefresh = false): Observable<UserMeResponse> {
    if (!forceRefresh && this.cachedMe) {
      return of(this.cachedMe);
    }

    return this.http.get<UserMeResponse>(withOpenApiBase(OPENAPI_PATHS.users.me)).pipe(
      tap((me) => {
        this.cachedMe = me;
      }),
    );
  }

  setCachedMe(me: UserMeResponse): void {
    this.cachedMe = me;
  }

  clearCachedMe(): void {
    this.cachedMe = null;
  }
}
