import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNextVersionComponent } from './create-next-version.component';

describe('CreateNextVersionComponent', () => {
  let component: CreateNextVersionComponent;
  let fixture: ComponentFixture<CreateNextVersionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateNextVersionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateNextVersionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
