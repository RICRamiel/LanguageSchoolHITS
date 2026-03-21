import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { NotificationAttachment, TeacherNotification } from '../../teacher-page.types';

@Component({
  selector: 'app-notification-details-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './notification-details-modal.component.html',
  styleUrl: './notification-details-modal.component.less',
})
export class NotificationDetailsModalComponent {
  readonly notification = input.required<TeacherNotification>();
  readonly uploading = input(false);
  readonly close = output<void>();
  readonly uploadAttachment = output<File>();
  readonly downloadAttachment = output<NotificationAttachment>();

  selectedFile: File | null = null;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    this.selectedFile = input?.files?.item(0) ?? null;
  }

  onUploadClick(): void {
    if (!this.selectedFile || this.uploading()) {
      return;
    }

    this.uploadAttachment.emit(this.selectedFile);
  }

  onDownloadClick(): void {
    const attachment = this.notification().attachment;
    if (!attachment?.id) {
      return;
    }

    this.downloadAttachment.emit(attachment);
  }
}
