import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ErrorToastService } from './error-toast.service';

const JWT_EXPIRED_REASON = 'JWT Token Expired';

export const httpErrorNotificationInterceptor: HttpInterceptorFn = (req, next) => {
  const errorToasts = inject(ErrorToastService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || isAuthRequest(req.url) || isExpiredJwtError(error)) {
        return throwError(() => error);
      }

      errorToasts.show(buildHttpErrorMessage(error), buildHttpErrorTitle(error), error.status);
      return throwError(() => error);
    }),
  );
};

function isAuthRequest(url: string): boolean {
  return /\/auth(?:\/|$)/.test(url);
}

function isExpiredJwtError(error: HttpErrorResponse): boolean {
  const body = error.error;
  return (
    error.status === 403 &&
    body !== null &&
    typeof body === 'object' &&
    (body as Record<string, unknown>)['access_denied_reason'] === JWT_EXPIRED_REASON
  );
}

function buildHttpErrorTitle(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'Сервер недоступен';
  }

  return `Ошибка запроса${error.status ? ` ${error.status}` : ''}`;
}

function buildHttpErrorMessage(error: HttpErrorResponse): string {
  const detail = extractErrorText(error.error);
  if (detail) {
    return detail;
  }

  if (error.status === 0) {
    return 'Не удалось подключиться к серверу. Проверьте сеть или доступность API.';
  }

  return error.message || 'Сервер вернул ошибку без подробного описания.';
}

function extractErrorText(payload: unknown): string | null {
  if (typeof payload === 'string') {
    return payload.trim() || null;
  }

  if (payload === null || typeof payload !== 'object') {
    return null;
  }

  const record = payload as Record<string, unknown>;
  for (const key of ['detail', 'message', 'error', 'title']) {
    const value = record[key];
    if (typeof value === 'string' && value.trim()) {
      return value.trim();
    }
  }

  const errors = record['errors'];
  if (Array.isArray(errors)) {
    const values = errors
      .filter((item): item is string => typeof item === 'string' && Boolean(item.trim()))
      .map((item) => item.trim());

    if (values.length > 0) {
      return values.join('\n');
    }
  }

  return null;
}
