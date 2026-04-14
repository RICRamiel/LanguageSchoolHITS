import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonComponent } from '../../../shared/ui/button/button.component';
import type { Group } from '../admin-page.models';

@Component({
  selector: 'app-assign-group-modal',
  standalone: true,
  imports: [FormsModule, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './assign-group-modal.component.html',
  styleUrl: './assign-group-modal.component.less',
})
export class AssignGroupModalComponent {
  private readonly cdr = inject(ChangeDetectorRef);

  readonly groups = input<Group[]>([]);
  readonly currentGroupId = input<number | null>(null);
  /** Курсы, в которых пользователь уже состоит (кнопка "Сохранить" неактивна при выборе такого курса). */
  readonly currentGroupIds = input<number[]>([]);
  readonly save = output<number>();
  readonly close = output<void>();

  selectedGroupId: number | null = null;

  isGroupAlreadyAssigned(groupId: number): boolean {
    const ids = this.currentGroupIds();
    if (ids.length > 0) return ids.includes(groupId);
    const single = this.currentGroupId();
    return single != null && single === groupId;
  }

  get groupsList(): Group[] {
    return this.groups();
  }

  onGroupChange(event: Event): void {
    const el = event.target as HTMLSelectElement;
    const v = el.value;
    this.selectedGroupId = v === '' || v === 'null' ? null : Number(v);
    this.cdr.markForCheck();
  }

  onSave(): void {
    const id = this.selectedGroupId;
    if (id == null) return;
    this.save.emit(Number(id));
  }

  onClose(): void {
    this.close.emit();
  }

  onOverlayClick(): void {
    this.onClose();
  }

  onModalClick(event: Event): void {
    event.stopPropagation();
  }
}
