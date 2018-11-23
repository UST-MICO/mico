import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailOverviewComponent } from './service-detail-overview.component';

describe('ServiceDetailOverviewComponent', () => {
  let component: ServiceDetailOverviewComponent;
  let fixture: ComponentFixture<ServiceDetailOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceDetailOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceDetailOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
