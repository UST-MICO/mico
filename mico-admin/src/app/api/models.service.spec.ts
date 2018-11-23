import { TestBed } from '@angular/core/testing';

import { ModelsService } from './models.service';

describe('ModelsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ModelsService = TestBed.get(ModelsService);
    expect(service).toBeTruthy();
  });
});
