import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoDataBooleanComponent } from './mico-data-boolean.component';

describe('MicoDataBooleanComponent', () => {
  let component: MicoDataBooleanComponent;
  let fixture: ComponentFixture<MicoDataBooleanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoDataBooleanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoDataBooleanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
