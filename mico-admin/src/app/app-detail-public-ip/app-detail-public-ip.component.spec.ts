import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDetailPublicIpComponent } from './app-detail-public-ip.component';

describe('AppDetailPublicIpComponent', () => {
  let component: AppDetailPublicIpComponent;
  let fixture: ComponentFixture<AppDetailPublicIpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppDetailPublicIpComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppDetailPublicIpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
