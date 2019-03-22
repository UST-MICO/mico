import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailKubeconfigComponent } from './service-detail-kubeconfig.component';

describe('ServiceDetailKubeconfigComponent', () => {
  let component: ServiceDetailKubeconfigComponent;
  let fixture: ComponentFixture<ServiceDetailKubeconfigComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceDetailKubeconfigComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceDetailKubeconfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
