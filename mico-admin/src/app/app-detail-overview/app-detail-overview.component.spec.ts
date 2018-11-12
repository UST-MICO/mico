import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDetailOverviewComponent } from './app-detail-overview.component';

describe('AppDetailOverviewComponent', () => {
  let component: AppDetailOverviewComponent;
  let fixture: ComponentFixture<AppDetailOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppDetailOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppDetailOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
