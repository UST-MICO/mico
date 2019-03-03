import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateServiceInterfaceComponent } from './update-service-interface.component';

describe('CreateServiceInterfaceComponent', () => {
  let component: UpdateServiceInterfaceComponent;
  let fixture: ComponentFixture<UpdateServiceInterfaceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateServiceInterfaceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateServiceInterfaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
