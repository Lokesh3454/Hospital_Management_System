import { Injectable } from '@angular/core';
import { JwtResponse } from '../../models/auth.model';

const TOKEN_KEY = 'hms_auth_token';
const USER_KEY = 'hms_auth_user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {

  constructor() { }

  public signOut(): void {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
  }

  public saveToken(token: string): void {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string | null {
    return window.localStorage.getItem(TOKEN_KEY);
  }

  public saveUser(user: JwtResponse): void {
    window.localStorage.removeItem(USER_KEY);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): JwtResponse | null {
    const user = window.localStorage.getItem(USER_KEY);
    if (user) {
      try {
        return JSON.parse(user);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  public isLoggedIn(): boolean {
    return !!this.getToken();
  }

  public getUserRoles(): string[] {
    const user = this.getUser();
    return user ? user.roles : [];
  }
}
