import { ChangeDetectionStrategy, Component, effect, input, output } from '@angular/core';
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
  readonly groups = input<Group[]>([]);
  readonly currentGroupId = input<number | null>(null);
  readonly save = output<number>();
  readonly close = output<void>();

  selectedGroupId: number | null = null;

  constructor() {
    effect(() => {
      this.selectedGroupId = this.currentGroupId();
    });
  }

  get groupsList(): Group[] {
    return this.groups();
  }

  onSave(): void {
    const id = this.selectedGroupId;
    if (id == null) return;
    this.save.emit(id);
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
