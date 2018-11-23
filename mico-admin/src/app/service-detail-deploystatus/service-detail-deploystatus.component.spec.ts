import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailDeploystatusComponent } from './service-detail-deploystatus.component';

describe('ServiceDetailDeploystatusComponent', () => {
  let component: ServiceDetailDeploystatusComponent;
  let fixture: ComponentFixture<ServiceDetailDeploystatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceDetailDeploystatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceDetailDeploystatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
