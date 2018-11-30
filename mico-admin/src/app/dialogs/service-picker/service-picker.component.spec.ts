import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServicePickerComponent } from './service-picker.component';

describe('ServicePickerComponent', () => {
  let component: ServicePickerComponent;
  let fixture: ComponentFixture<ServicePickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServicePickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServicePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
