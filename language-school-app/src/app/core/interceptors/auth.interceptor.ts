import { HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthTokenService } from '../auth/auth-token.service';

export function authInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const authTokenService = inject(AuthTokenService);
  const token = authTokenService.token();

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }

  return next(req);
}
