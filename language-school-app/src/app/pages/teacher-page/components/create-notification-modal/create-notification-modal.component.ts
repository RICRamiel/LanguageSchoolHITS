import { Component, effect, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CreateNotificationPayload, TeacherGroup } from '../../teacher-page.types';

@Component({
  selector: 'app-create-notification-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './create-notification-modal.component.html',
  styleUrl: './create-notification-modal.component.less',
})
export class CreateNotificationModalComponent {
  readonly groups = input<TeacherGroup[]>([]);

  title = '';
  content = '';
  groupId = '';

  readonly close = output<void>();
  readonly submit = output<CreateNotificationPayload>();

  constructor() {
    effect(() => {
      const groups = this.groups();
      if (!groups.length) {
        this.groupId = '';
        return;
      }

      const exists = groups.some((group) => String(group.id) === this.groupId);
      if (!exists) {
        this.groupId = String(groups[0].id);
      }
    });
  }

  onSubmit() {
    const selectedGroup = this.resolveGroup();
    if (!selectedGroup) {
      return;
    }

    this.submit.emit({
      title: this.title,
      content: this.content,
      groupId: selectedGroup.id,
    });
  }

  private resolveGroup(): TeacherGroup | null {
    const groups = this.groups();
    if (!groups.length) {
      return null;
    }

    const selected = groups.find((group) => String(group.id) === this.groupId);
    return selected ?? groups[0];
  }
}
