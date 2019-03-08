import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDependencyGraphComponent } from './service-dependency-graph.component';

describe('ServiceDependencyGraphComponent', () => {
    let component: ServiceDependencyGraphComponent;
    let fixture: ComponentFixture<ServiceDependencyGraphComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ServiceDependencyGraphComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ServiceDependencyGraphComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
