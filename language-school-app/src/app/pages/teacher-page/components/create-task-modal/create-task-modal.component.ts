import { Component, effect, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CreateTaskPayload, TeacherGroup } from '../../teacher-page.types';

@Component({
  selector: 'app-create-task-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './create-task-modal.component.html',
  styleUrl: './create-task-modal.component.less',
})
export class CreateTaskModalComponent {
  readonly assignmentTypes: Array<{ value: CreateTaskPayload['assignmentType']; label: string }> = [
    { value: 'INDIVIDUAL', label: 'Индивидуальное' },
    { value: 'TEAM', label: 'Командное' },
  ];
  readonly teamTypes: Array<{ value: CreateTaskPayload['teamType']; label: string }> = [
    { value: 'RANDOM', label: 'Случайный' },
    { value: 'DRAFT', label: 'Драфт' },
    { value: 'FREEROAM', label: 'Свободный' },
    { value: 'CUSTOM', label: 'Кастомный' },
  ];
  readonly resolveTypes: Array<{ value: CreateTaskPayload['resolveType']; label: string }> = [
    { value: 'FIRST_SUBMITTED_SOLUTION', label: 'Первое решение' },
    { value: 'LAST_SUBMITTED_SOLUTION', label: 'Последнее решение' },
    { value: 'CAPTAINS_SOLUTION', label: 'Решение капитана' },
    { value: 'MOST_VOTES_SOLUTION', label: 'Демократическое большинство' },
    { value: 'AT_LEAST_VOTES_SOLUTION', label: 'Квалифицированное большинство' },
  ];

  readonly groups = input<TeacherGroup[]>([]);

  title = '';
  description = '';
  dueDate = '';
  assignmentType: CreateTaskPayload['assignmentType'] = 'TEAM';
  teamType: CreateTaskPayload['teamType'] = 'FREEROAM';
  resolveType: CreateTaskPayload['resolveType'] = 'LAST_SUBMITTED_SOLUTION';
  minTeamSize: number | null = 2;
  maxTeamSize: number | null = 5;
  minTeamsAmount: number | null = 1;
  maxTeamsAmount: number | null = 5;
  votesThreshold: number | null = null;
  validationError = '';
  groupId = '';
  groupQuery = '';
  isGroupListOpen = false;

  readonly close = output<void>();
  readonly submit = output<CreateTaskPayload>();

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
    if (!selectedGroup || !this.validate()) {
      return;
    }

    const isTeamTask = this.assignmentType === 'TEAM';
    const needsVotesThreshold = this.resolveType === 'AT_LEAST_VOTES_SOLUTION';

    this.submit.emit({
      title: this.title.trim(),
      description: this.description.trim(),
      dueDate: this.dueDate,
      groupId: selectedGroup.id,
      groupName: selectedGroup.name,
      assignmentType: this.assignmentType,
      teamType: this.teamType,
      resolveType: this.resolveType,
      minTeamSize: isTeamTask ? this.minTeamSize : null,
      maxTeamSize: isTeamTask ? this.maxTeamSize : null,
      minTeamsAmount: isTeamTask ? this.minTeamsAmount : null,
      maxTeamsAmount: isTeamTask ? this.maxTeamsAmount : null,
      votesThreshold: needsVotesThreshold ? this.votesThreshold : null,
    });
  }

  onAssignmentTypeChange(value: string): void {
    this.assignmentType = value === 'INDIVIDUAL' ? 'INDIVIDUAL' : 'TEAM';
    if (this.assignmentType === 'INDIVIDUAL') {
      this.minTeamSize = null;
      this.maxTeamSize = null;
      this.minTeamsAmount = null;
      this.maxTeamsAmount = null;
    } else {
      this.minTeamSize ??= 2;
      this.maxTeamSize ??= 5;
      this.minTeamsAmount ??= 1;
      this.maxTeamsAmount ??= 5;
    }
    this.validationError = '';
  }

  onResolveTypeChange(value: string): void {
    this.resolveType = this.resolveTypes.some((item) => item.value === value)
      ? (value as CreateTaskPayload['resolveType'])
      : 'LAST_SUBMITTED_SOLUTION';
    if (this.resolveType !== 'AT_LEAST_VOTES_SOLUTION') {
      this.votesThreshold = null;
    } else {
      this.votesThreshold ??= 1;
    }
    this.validationError = '';
  }

  onNumberInput(
    field:
      | 'minTeamSize'
      | 'maxTeamSize'
      | 'minTeamsAmount'
      | 'maxTeamsAmount'
      | 'votesThreshold',
    value: string,
  ): void {
    const normalized = value.trim();
    if (!normalized) {
      this[field] = null;
      this.validationError = '';
      return;
    }

    const parsed = Number.parseInt(normalized, 10);
    this[field] = Number.isFinite(parsed) ? parsed : null;
    this.validationError = '';
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

  private validate(): boolean {
    if (!this.title.trim()) {
      this.validationError = 'Введите название задания.';
      return false;
    }
    if (!this.description.trim()) {
      this.validationError = 'Введите описание задания.';
      return false;
    }
    if (!this.dueDate) {
      this.validationError = 'Выберите дедлайн.';
      return false;
    }

    if (this.assignmentType === 'TEAM') {
      if (!this.isPositiveNumber(this.minTeamSize) || !this.isPositiveNumber(this.maxTeamSize)) {
        this.validationError = 'Укажите размер команды (min/max).';
        return false;
      }
      if ((this.minTeamSize as number) > (this.maxTeamSize as number)) {
        this.validationError = 'Минимум людей в команде не может быть больше максимума.';
        return false;
      }
      if (!this.isPositiveNumber(this.minTeamsAmount) || !this.isPositiveNumber(this.maxTeamsAmount)) {
        this.validationError = 'Укажите количество команд (min/max).';
        return false;
      }
      if ((this.minTeamsAmount as number) > (this.maxTeamsAmount as number)) {
        this.validationError = 'Минимум команд не может быть больше максимума.';
        return false;
      }
    }

    if (
      this.resolveType === 'AT_LEAST_VOTES_SOLUTION' &&
      !this.isPositiveNumber(this.votesThreshold)
    ) {
      this.validationError = 'Для квалифицированного большинства задайте порог голосов.';
      return false;
    }

    this.validationError = '';
    return true;
  }

  private isPositiveNumber(value: number | null): value is number {
    return Number.isFinite(value) && (value as number) > 0;
  }
}
