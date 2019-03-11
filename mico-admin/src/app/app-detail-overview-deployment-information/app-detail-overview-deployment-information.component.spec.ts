import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDetailOverviewDeploymentInformationComponent } from './app-detail-overview-deployment-information.component';

describe('AppDetailOverviewDeploymentInformationComponent', () => {
  let component: AppDetailOverviewDeploymentInformationComponent;
  let fixture: ComponentFixture<AppDetailOverviewDeploymentInformationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppDetailOverviewDeploymentInformationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppDetailOverviewDeploymentInformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
