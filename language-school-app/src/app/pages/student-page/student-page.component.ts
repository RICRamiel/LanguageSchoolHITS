import { Component } from '@angular/core';
import {HeaderComponent} from '../../shared/ui/header/header.component';
import {TabsComponent} from '../../shared/ui/tabs/tabs.component';
import {TaskCardComponent} from '../../shared/ui/task-card/task-card.component';

@Component({
  selector: 'app-student-page',
  standalone: true,
  imports: [
    HeaderComponent,
    TabsComponent,
    TaskCardComponent,
  ],
  templateUrl: './student-page.component.html',
  styleUrl: './student-page.component.less',
})
export class StudentPageComponent {

  tabs = [
    { id: 'tasks', label: 'Задания' },
    { id: 'notifications', label: 'Уведомления', badge: 2 },
  ];

  activeTab = 'tasks';

  tasks = [
    {
      title: 'Эссе по грамматике',
      pillText: 'Сдано',
      pillVariant: 'success' as const,
      teacher: 'Смирнова Е.В.',
      description:
        'Написать эссе на тему Present Perfect. Объем: 300–500 слов. Используйте примеры из реальной жизни.',
      dueText: '20 февраля 2026',
    },
    {
      title: 'Устное задание',
      pillText: null,
      pillVariant: 'neutral' as const,
      teacher: 'Смирнова Е.В.',
      description:
        'Подготовить 3-минутный монолог о своем хобби. Запишите видео и загрузите.',
      dueText: '25 февраля 2026',
    },
  ];

  openTaskDetails(taskTitle: string) {
    console.log('details:', taskTitle);
  }



  protected onLogout = () =>{
    console.log('logout');
  };
}
