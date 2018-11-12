import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings.component';

describe('AppDetailDeploysettingsComponent', () => {
  let component: AppDetailDeploysettingsComponent;
  let fixture: ComponentFixture<AppDetailDeploysettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppDetailDeploysettingsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppDetailDeploysettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
