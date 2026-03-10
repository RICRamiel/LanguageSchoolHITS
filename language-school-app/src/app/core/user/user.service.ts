import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserMeResponse } from './user.models';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getMe(): Observable<UserMeResponse> {
    return this.http.get<UserMeResponse>(withOpenApiBase(OPENAPI_PATHS.users.me));
  }
}
