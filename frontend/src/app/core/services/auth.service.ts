import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, RegisterRequest, JwtResponse, ApiResponse } from '../../models/auth.model';
import { TokenStorageService } from './token-storage.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authUrl = `${environment.apiUrl}/auth`;

  private currentUserSubject = new BehaviorSubject<JwtResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService,
    private router: Router
  ) {
    const storedUser = this.tokenStorage.getUser();
    if (storedUser && this.tokenStorage.isLoggedIn()) {
      this.currentUserSubject.next(storedUser);
    }
  }

  public get currentUserValue(): JwtResponse | null {
    return this.currentUserSubject.value;
  }

  public get currentUser(): JwtResponse | null {
    return this.currentUserSubject.value;
  }

  public login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.authUrl}/login`, credentials).pipe(
      tap(response => {
        this.tokenStorage.saveToken(response.token);
        this.tokenStorage.saveUser(response);
        this.currentUserSubject.next(response);
      })
    );
  }

  public register(user: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.authUrl}/register`, user);
  }

  public logout(): void {
    this.tokenStorage.signOut();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  public isLoggedIn(): boolean {
    return this.tokenStorage.isLoggedIn();
  }

  public hasRole(role: string): boolean {
    const user = this.currentUserValue;
    if (!user || !user.roles) return false;
    const formattedRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return user.roles.includes(formattedRole);
  }

  public hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  public getDashboardRouteForUser(): string {
    const user = this.currentUserValue;
    if (!user || !user.roles || user.roles.length === 0) {
      return '/login';
    }
    if (user.roles.includes('ROLE_ADMIN')) {
      return '/admin';
    } else if (user.roles.includes('ROLE_DOCTOR')) {
      return '/doctor';
    } else if (user.roles.includes('ROLE_RECEPTIONIST')) {
      return '/receptionist';
    } else if (user.roles.includes('ROLE_PATIENT')) {
      return '/patient';
    }
    return '/login';
  }
}
