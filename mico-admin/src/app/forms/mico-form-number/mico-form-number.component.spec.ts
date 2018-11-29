import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoFormNumberComponent } from './mico-form-number.component';

describe('MicoFormNumberComponent', () => {
  let component: MicoFormNumberComponent;
  let fixture: ComponentFixture<MicoFormNumberComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoFormNumberComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoFormNumberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
