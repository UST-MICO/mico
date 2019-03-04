import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeServiceVersionComponent } from './change-service-version.component';

describe('ChangeServiceVersionComponent', () => {
  let component: ChangeServiceVersionComponent;
  let fixture: ComponentFixture<ChangeServiceVersionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ChangeServiceVersionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangeServiceVersionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
