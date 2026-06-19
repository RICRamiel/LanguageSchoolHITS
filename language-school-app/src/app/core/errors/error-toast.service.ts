import { Injectable, signal } from '@angular/core';

export type ErrorToast = {
  id: number;
  title: string;
  message: string;
  status?: number;
};

const TOAST_TIMEOUT_MS = 7000;

@Injectable({ providedIn: 'root' })
export class ErrorToastService {
  private nextId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly toastsSignal = signal<ErrorToast[]>([]);

  readonly toasts = this.toastsSignal.asReadonly();

  show(message: string, title = 'Ошибка', status?: number): void {
    const id = this.nextId++;
    const toast: ErrorToast = { id, title, message, status };

    this.toastsSignal.update((items) => [toast, ...items].slice(0, 4));

    const timer = setTimeout(() => this.dismiss(id), TOAST_TIMEOUT_MS);
    this.timers.set(id, timer);
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }

    this.toastsSignal.update((items) => items.filter((toast) => toast.id !== id));
  }
}
