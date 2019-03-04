import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeploymentInformationDialogComponent } from './deployment-information-dialog.component';

describe('DeploymentInformationDialogComponent', () => {
  let component: DeploymentInformationDialogComponent;
  let fixture: ComponentFixture<DeploymentInformationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeploymentInformationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeploymentInformationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
