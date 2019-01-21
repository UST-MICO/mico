import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateApplicationInterfaceComponent } from './create-application-interface.component';

describe('CreateApplicationInterfaceComponent', () => {
  let component: CreateApplicationInterfaceComponent;
  let fixture: ComponentFixture<CreateApplicationInterfaceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateApplicationInterfaceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateApplicationInterfaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
