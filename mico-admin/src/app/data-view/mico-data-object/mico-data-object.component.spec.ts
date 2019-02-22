import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoDataObjectComponent } from './mico-data-object.component';

describe('MicoDataObjectComponent', () => {
  let component: MicoDataObjectComponent;
  let fixture: ComponentFixture<MicoDataObjectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoDataObjectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoDataObjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
