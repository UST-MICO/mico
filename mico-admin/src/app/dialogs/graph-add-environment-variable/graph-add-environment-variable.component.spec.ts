import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphAddEnvironmentVariableComponent } from './graph-add-environment-variable.component';

describe('GraphAddEnvironmentVariableComponent', () => {
  let component: GraphAddEnvironmentVariableComponent;
  let fixture: ComponentFixture<GraphAddEnvironmentVariableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GraphAddEnvironmentVariableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GraphAddEnvironmentVariableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
