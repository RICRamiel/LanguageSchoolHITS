import { Component, OnInit, input, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { TeacherTask, TeacherTaskDetailsSection, TeacherTaskSubmission } from '../../teacher-page.types';

@Component({
  selector: 'app-task-details-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-details-modal.component.html',
  styleUrl: './task-details-modal.component.less',
})
export class TaskDetailsModalComponent implements OnInit {
  readonly task = input.required<TeacherTask>();
  readonly activeSection = input<TeacherTaskDetailsSection>('overview');
  readonly close = output<void>();
  readonly submitComment = output<string>();
  readonly downloadAttachment = output<TeacherTaskSubmission>();
  readonly selectedSection = signal<TeacherTaskDetailsSection>('overview');
  readonly commentText = signal('');

  ngOnInit() {
    this.selectedSection.set(this.activeSection());
  }

  setSection(section: TeacherTaskDetailsSection) {
    this.selectedSection.set(section);
  }

  onCommentInput(event: Event): void {
    const target = event.target as HTMLTextAreaElement | null;
    this.commentText.set(target?.value ?? '');
  }

  onCommentSubmit(): void {
    const value = this.commentText().trim();
    if (!value) {
      return;
    }
    this.submitComment.emit(value);
    this.commentText.set('');
  }

  requestAttachmentDownload(work: TeacherTaskSubmission): void {
    if (!work.id) {
      return;
    }
    this.downloadAttachment.emit(work);
  }
}
