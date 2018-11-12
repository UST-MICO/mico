import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDetailDeploystatusComponent } from './app-detail-deploystatus.component';

describe('AppDetailDeploystatusComponent', () => {
  let component: AppDetailDeploystatusComponent;
  let fixture: ComponentFixture<AppDetailDeploystatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppDetailDeploystatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppDetailDeploystatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
