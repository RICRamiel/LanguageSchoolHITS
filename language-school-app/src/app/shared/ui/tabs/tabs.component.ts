import { Component, effect, input, output, signal } from '@angular/core';

export type TabItem = {
  id: string;
  label: string;
  badge?: number;
  disabled?: boolean;
};

@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [],
  templateUrl: './tabs.component.html',
  styleUrl: './tabs.component.less',
})
export class TabsComponent {
  readonly tabs = input<TabItem[]>([]);
  readonly activeId = input<string | null>(null);
  readonly stretch = input<boolean>(true);

  readonly tabChange = output<string>();

  private readonly _active = signal<string>('');

  constructor() {
    effect(() => {
      const tabs = this.tabs();
      const incoming = this.activeId();

      if (!tabs.length) {
        this._active.set('');
        return;
      }

      const next =
        incoming && tabs.some((t) => t.id === incoming) ? incoming : tabs[0].id;

      this._active.set(next);
    });
  }

  isActive(id: string) {
    return this._active() === id;
  }

  select(tab: TabItem) {
    if (tab.disabled) return;
    this._active.set(tab.id);
    this.tabChange.emit(tab.id);
  }
}
