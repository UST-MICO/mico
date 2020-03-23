import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatternListComponent } from './pattern-list.component';

describe('PatternListComponent', () => {
  let component: PatternListComponent;
  let fixture: ComponentFixture<PatternListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatternListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatternListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
