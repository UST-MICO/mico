import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDetailStatusComponent } from './app-detail-status.component';

describe('AppDetailStatusComponent', () => {
  let component: AppDetailStatusComponent;
  let fixture: ComponentFixture<AppDetailStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppDetailStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppDetailStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
