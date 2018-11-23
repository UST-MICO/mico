import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateServiceDialogComponent } from './create-service.component';

describe('CreateServiceDialogComponent', () => {
  let component: CreateServiceDialogComponent;
  let fixture: ComponentFixture<CreateServiceDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateServiceDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateServiceDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  expect(component).toBeTruthy();
      it('should create', () => {
  });
});
