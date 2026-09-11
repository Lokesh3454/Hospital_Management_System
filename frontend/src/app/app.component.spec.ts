import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './core/services/auth.service';
import { SidebarService } from './core/services/sidebar.service';

import { of } from 'rxjs';

describe('AppComponent', () => {
  let mockAuthService: any;
  let mockSidebarService: jasmine.SpyObj<SidebarService>;

  beforeEach(async () => {
    mockAuthService = {
      isLoggedIn: jasmine.createSpy('isLoggedIn').and.returnValue(false),
      hasRole: jasmine.createSpy('hasRole').and.returnValue(false),
      getDashboardRouteForUser: jasmine.createSpy('getDashboardRouteForUser').and.returnValue('/login'),
      logout: jasmine.createSpy('logout'),
      currentUser$: of(null),
      currentUserValue: null,
      currentUser: null
    };
    mockSidebarService = jasmine.createSpyObj('SidebarService', ['isMobileOpen', 'close', 'toggle']);

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService },
        { provide: SidebarService, useValue: mockSidebarService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'MedPulse HMS' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('MedPulse HMS');
  });

  it('should render main content container with router-outlet', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });
});
