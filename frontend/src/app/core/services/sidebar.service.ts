import { Injectable, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  isMobileOpen = signal<boolean>(false);

  constructor(private router: Router) {
    // Automatically close mobile sidebar when navigating between pages
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.close();
    });
  }

  toggle(): void {
    this.isMobileOpen.update(open => !open);
  }

  open(): void {
    this.isMobileOpen.set(true);
  }

  close(): void {
    this.isMobileOpen.set(false);
  }
}
