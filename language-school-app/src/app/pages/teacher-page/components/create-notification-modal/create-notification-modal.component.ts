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
  groupQuery = '';
  isGroupListOpen = false;
  attachmentFile: File | null = null;

  readonly close = output<void>();
  readonly submit = output<CreateNotificationPayload>();

  constructor() {
    effect(() => {
      const groups = this.groups();
      if (!groups.length) {
        this.groupId = '';
        this.groupQuery = '';
        this.isGroupListOpen = false;
        return;
      }

      const exists = groups.some((group) => String(group.id) === this.groupId);
      if (!exists) {
        this.groupId = String(groups[0].id);
      }

      if (!this.groupQuery.trim()) {
        const selected = groups.find((group) => String(group.id) === this.groupId);
        this.groupQuery = selected?.name ?? '';
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
      attachmentFile: this.attachmentFile,
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files?.item(0) ?? null;
    this.attachmentFile = file;
  }

  toggleGroupList(): void {
    if (!this.groups().length) {
      return;
    }
    this.isGroupListOpen = !this.isGroupListOpen;
  }

  onGroupQueryInput(value: string): void {
    this.groupQuery = value;
    this.isGroupListOpen = true;
  }

  selectGroup(group: TeacherGroup): void {
    this.groupId = String(group.id);
    this.groupQuery = group.name;
    this.isGroupListOpen = false;
  }

  get filteredGroups(): TeacherGroup[] {
    const query = this.groupQuery.trim().toLowerCase();
    if (!query) {
      return this.groups();
    }

    return this.groups().filter((group) => group.name.toLowerCase().includes(query));
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
