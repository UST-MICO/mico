import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailStatusComponent } from './service-detail-status.component';

describe('ServiceDetailStatusComponent', () => {
  let component: ServiceDetailStatusComponent;
  let fixture: ComponentFixture<ServiceDetailStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceDetailStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceDetailStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
