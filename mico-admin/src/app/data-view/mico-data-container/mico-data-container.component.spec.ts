import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MicoDataContainerComponent } from './mico-data-container.component';

describe('MicoDataContainerComponent', () => {
  let component: MicoDataContainerComponent;
  let fixture: ComponentFixture<MicoDataContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MicoDataContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicoDataContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
