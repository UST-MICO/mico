import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoFormComponent } from './mico-form.component';

describe('MicoFormComponent', () => {
  let component: MicoFormComponent;
  let fixture: ComponentFixture<MicoFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
