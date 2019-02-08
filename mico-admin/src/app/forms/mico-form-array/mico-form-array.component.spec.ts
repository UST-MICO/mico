import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoFormArrayComponent } from './mico-form-array.component';

describe('MicoFormBooleanComponent', () => {
  let component: MicoFormArrayComponent;
  let fixture: ComponentFixture<MicoFormArrayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoFormArrayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoFormArrayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
