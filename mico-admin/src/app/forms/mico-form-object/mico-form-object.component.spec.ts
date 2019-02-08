import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoFormObjectComponent } from './mico-form-object.component';

describe('MicoFormBooleanComponent', () => {
  let component: MicoFormObjectComponent;
  let fixture: ComponentFixture<MicoFormObjectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoFormObjectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoFormObjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
