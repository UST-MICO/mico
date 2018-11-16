import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoFormQuestionComponent } from './mico-form-question.component';

describe('MicoFormQuestionComponent', () => {
  let component: MicoFormQuestionComponent;
  let fixture: ComponentFixture<MicoFormQuestionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoFormQuestionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoFormQuestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
