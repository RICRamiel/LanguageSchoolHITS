import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

const TOKEN_KEY = 'auth_token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);
  const router = inject(Router);
  const isBrowser = isPlatformBrowser(platformId);
  const token = isBrowser ? localStorage.getItem(TOKEN_KEY) : null;

  const outgoing = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(outgoing).pipe(
    catchError((err: unknown) => {
      if (isBrowser && err instanceof HttpErrorResponse && err.status === 403) {
        const body = err.error as Record<string, unknown> | null;
        if (body?.['access_denied_reason'] === 'JWT Token Expired') {
          localStorage.removeItem(TOKEN_KEY);
          void router.navigateByUrl('/');
        }
      }
      return throwError(() => err);
    }),
  );
};
