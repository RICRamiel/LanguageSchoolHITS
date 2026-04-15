import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { StudentTask, StudentTaskComment } from '../../student-page.types';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-student-task-details-modal',
  standalone: true,
  imports: [FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './student-task-details-modal.component.html',
  styleUrl: './student-task-details-modal.component.less',
})
export class StudentTaskDetailsModalComponent {
  readonly task = input.required<StudentTask>();
  readonly uploadInProgress = input<boolean>(false);
  readonly completeInProgress = input<boolean>(false);
  readonly commentsLoading = input<boolean>(false);
  readonly commentSubmitting = input<boolean>(false);
  readonly uploadedFileLink = input<string | null>(null);
  readonly comments = input<StudentTaskComment[]>([]);
  readonly teamActionInProgress = input<boolean>(false);
  readonly close = output<void>();
  readonly uploadFile = output<File>();
  readonly completeTask = output<void>();
  readonly submitComment = output<string>();
  readonly createTeam = output<string>();
  readonly joinTeam = output<string>();

  readonly selectedFile = signal<File | null>(null);
  readonly commentText = signal('');
  readonly newTeamName = signal('');

  onFileChange(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    const file = target?.files?.[0] ?? null;
    this.selectedFile.set(file);
  }

  submitFile(): void {
    const file = this.selectedFile();
    if (!file || this.uploadInProgress()) {
      return;
    }
    this.uploadFile.emit(file);
  }

  submitCompleteTask(): void {
    if (this.completeInProgress()) {
      return;
    }
    this.completeTask.emit();
  }

  onCommentInput(event: Event): void {
    const target = event.target as HTMLTextAreaElement | null;
    this.commentText.set((target?.value ?? '').trimStart());
  }

  sendComment(): void {
    const text = this.commentText().trim();
    if (!text || this.commentSubmitting()) {
      return;
    }

    this.submitComment.emit(text);
    this.commentText.set('');
  }

  onTeamNameInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.newTeamName.set(target?.value ?? '');
  }

  onCreateTeam(): void {
    const name = this.newTeamName().trim();
    if (!name || this.teamActionInProgress()) {
      return;
    }
    this.createTeam.emit(name);
    this.newTeamName.set('');
  }

  onJoinTeam(teamId: string): void {
    if (this.teamActionInProgress()) {
      return;
    }
    this.joinTeam.emit(teamId);
  }
}
