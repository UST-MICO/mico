import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoDataStringComponent } from './mico-data-string.component';

describe('MicoDataStringComponent', () => {
  let component: MicoDataStringComponent;
  let fixture: ComponentFixture<MicoDataStringComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoDataStringComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoDataStringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
