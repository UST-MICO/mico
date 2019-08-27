import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphAddKafkaTopicComponent } from './graph-add-kafka-topic.component';

describe('GraphAddKafkaTopicComponent', () => {
  let component: GraphAddKafkaTopicComponent;
  let fixture: ComponentFixture<GraphAddKafkaTopicComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GraphAddKafkaTopicComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GraphAddKafkaTopicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
