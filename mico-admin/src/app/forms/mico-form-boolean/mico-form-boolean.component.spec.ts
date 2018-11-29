import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoFormBooleanComponent } from './mico-form-boolean.component';

describe('MicoFormBooleanComponent', () => {
  let component: MicoFormBooleanComponent;
  let fixture: ComponentFixture<MicoFormBooleanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoFormBooleanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoFormBooleanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
