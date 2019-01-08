import { TestBed } from '@angular/core/testing';

import { ApiBaseFunctionService } from './api-base-function.service';

describe('ApiBaseFunctionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ApiBaseFunctionService = TestBed.get(ApiBaseFunctionService);
    expect(service).toBeTruthy();
  });
});
