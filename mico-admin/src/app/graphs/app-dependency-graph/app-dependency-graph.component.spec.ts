import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppDependencyGraphComponent } from './app-dependency-graph.component';

describe('AppDependencyGraphComponent', () => {
    let component: AppDependencyGraphComponent;
    let fixture: ComponentFixture<AppDependencyGraphComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [AppDependencyGraphComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppDependencyGraphComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
