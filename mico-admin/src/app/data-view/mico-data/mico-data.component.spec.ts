import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoDataComponent } from './mico-data.component';

describe('MicoDataComponent', () => {
  let component: MicoDataComponent;
  let fixture: ComponentFixture<MicoDataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
