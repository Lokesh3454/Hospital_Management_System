/**
 * Utility functions to ensure pages and scrolling containers always start at the top (scrollY = 0).
 * Used during route transitions, login completions, and portal entries.
 */
export function scrollToTop(): void {
  if (typeof window !== 'undefined') {
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
  }
  if (typeof document !== 'undefined') {
    if (document.documentElement) {
      document.documentElement.scrollTop = 0;
    }
    if (document.body) {
      document.body.scrollTop = 0;
    }
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
      mainContent.scrollTop = 0;
    }
    const mainTag = document.querySelector('main');
    if (mainTag) {
      mainTag.scrollTop = 0;
    }
  }
}

/**
 * Robust scroll-to-top that executes immediately, in the next animation frame,
 * and after microtasks/asynchronous template rendering.
 */
export function forceScrollToTop(): void {
  scrollToTop();
  if (typeof requestAnimationFrame !== 'undefined') {
    requestAnimationFrame(() => scrollToTop());
  }
  setTimeout(() => scrollToTop(), 0);
  setTimeout(() => scrollToTop(), 50);
  setTimeout(() => scrollToTop(), 150);
}
