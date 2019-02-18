import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoDataArrayComponent } from './mico-data-array.component';

describe('MicoDataArrayComponent', () => {
  let component: MicoDataArrayComponent;
  let fixture: ComponentFixture<MicoDataArrayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoDataArrayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoDataArrayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
