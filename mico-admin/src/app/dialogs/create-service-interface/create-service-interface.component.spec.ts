import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateServiceInterfaceComponent } from './create-service-interface.component';

describe('CreateServiceInterfaceComponent', () => {
  let component: CreateServiceInterfaceComponent;
  let fixture: ComponentFixture<CreateServiceInterfaceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateServiceInterfaceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateServiceInterfaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
