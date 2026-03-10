import { Component, OnInit, input, output, signal } from '@angular/core';
import { TeacherTask, TeacherTaskDetailsSection } from '../../teacher-page.types';

@Component({
  selector: 'app-task-details-modal',
  standalone: true,
  imports: [],
  templateUrl: './task-details-modal.component.html',
  styleUrl: './task-details-modal.component.less',
})
export class TaskDetailsModalComponent implements OnInit {
  readonly task = input.required<TeacherTask>();
  readonly activeSection = input<TeacherTaskDetailsSection>('overview');
  readonly close = output<void>();
  readonly selectedSection = signal<TeacherTaskDetailsSection>('overview');

  ngOnInit() {
    this.selectedSection.set(this.activeSection());
  }

  setSection(section: TeacherTaskDetailsSection) {
    this.selectedSection.set(section);
  }

  downloadTask() {
    const task = this.task();
    const content = [
      `Задание: ${task.title}`,
      `Группа: ${task.group}`,
      `Срок: ${task.dueDate}`,
      '',
      task.description,
    ].join('\n');
    this.downloadFile(this.toFileName(task.title), content);
  }

  downloadSubmission(fileName: string, content: string) {
    this.downloadFile(fileName, content);
  }

  private downloadFile(fileName: string, content: string) {
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  }

  private toFileName(title: string): string {
    return `${title.replace(/[^\p{L}\p{N}]+/gu, '-').replace(/^-+|-+$/g, '') || 'task'}.txt`;
  }
}
