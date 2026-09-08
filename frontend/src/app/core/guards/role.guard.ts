import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRoles: string[] = route.data['expectedRoles'] || [];
  if (expectedRoles.length === 0) {
    return true;
  }

  const hasRole = expectedRoles.some(role => authService.hasRole(role));
  if (hasRole) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};
