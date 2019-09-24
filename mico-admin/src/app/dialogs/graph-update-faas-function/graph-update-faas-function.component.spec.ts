import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphUpdateFaasFunctionComponent } from './graph-update-faas-function.component';

describe('GraphUpdateFaasFunctionComponent', () => {
  let component: GraphUpdateFaasFunctionComponent;
  let fixture: ComponentFixture<GraphUpdateFaasFunctionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GraphUpdateFaasFunctionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GraphUpdateFaasFunctionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
