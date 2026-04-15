import { Component, OnInit, input, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { TeacherTask, TaskTeam, TeacherTaskDetailsSection, TeacherTaskSubmission } from '../../teacher-page.types';

export type CourseStudent = { id: string; fullName: string };

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
  readonly courseStudents = input<CourseStudent[]>([]);
  readonly teamCreating = input<boolean>(false);
  readonly studentAdding = input<boolean>(false);
  readonly close = output<void>();
  readonly submitComment = output<string>();
  readonly downloadAttachment = output<TeacherTaskSubmission>();
  readonly createTeam = output<string>();
  readonly addStudentToTeam = output<{ teamId: string; studentId: string }>();
  readonly selectedSection = signal<TeacherTaskDetailsSection>('overview');
  readonly commentText = signal('');
  readonly newTeamName = signal('');
  readonly selectedStudentForTeam = signal<Record<string, string>>({});

  ngOnInit(): void {
    this.selectedSection.set(this.activeSection());
  }

  setSection(section: TeacherTaskDetailsSection): void {
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

  onTeamNameInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.newTeamName.set(target?.value ?? '');
  }

  onCreateTeam(): void {
    const name = this.newTeamName().trim();
    if (!name || this.teamCreating()) {
      return;
    }
    this.createTeam.emit(name);
    this.newTeamName.set('');
  }

  getSelectedStudent(teamId: string): string {
    return this.selectedStudentForTeam()[teamId] ?? '';
  }

  onStudentSelect(teamId: string, studentId: string): void {
    this.selectedStudentForTeam.update((map) => ({ ...map, [teamId]: studentId }));
  }

  onAddStudentToTeam(teamId: string): void {
    const studentId = this.selectedStudentForTeam()[teamId];
    if (!studentId || this.studentAdding()) {
      return;
    }
    this.addStudentToTeam.emit({ teamId, studentId });
    this.selectedStudentForTeam.update((map) => ({ ...map, [teamId]: '' }));
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
