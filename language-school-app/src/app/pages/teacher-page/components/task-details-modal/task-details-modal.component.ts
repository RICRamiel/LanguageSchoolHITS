import {Component, OnInit, input, output, signal, ChangeDetectionStrategy} from '@angular/core';
import {TeacherTask, TeacherTaskDetailsSection, TeacherTaskSubmission} from '../../teacher-page.types';

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

  getAssignmentTypeLabel(value: TeacherTask['assignmentType']): string {
    return value === 'INDIVIDUAL' ? 'Индивидуальное' : 'Командное';
  }

  getTeamTypeLabel(value: TeacherTask['teamType']): string {
    switch (value) {
      case 'RANDOM':
        return 'Случайный';
      case 'DRAFT':
        return 'Драфт';
      case 'CUSTOM':
        return 'Кастомный';
      case 'FREEROAM':
      default:
        return 'Свободный';
    }
  }

  getResolveTypeLabel(value: TeacherTask['resolveType']): string {
    switch (value) {
      case 'FIRST_SUBMITTED_SOLUTION':
        return 'Первое решение';
      case 'CAPTAINS_SOLUTION':
        return 'Решение капитана';
      case 'MOST_VOTES_SOLUTION':
        return 'Демократическое большинство';
      case 'AT_LEAST_VOTES_SOLUTION':
        return 'Квалифицированное большинство';
      case 'LAST_SUBMITTED_SOLUTION':
      default:
        return 'Последнее решение';
    }
  }
}
