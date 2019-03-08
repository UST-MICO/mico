import { TestBed } from '@angular/core/testing';

import { UtilServiceService } from './util-service.service';

describe('UtilServiceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UtilServiceService = TestBed.get(UtilServiceService);
    expect(service).toBeTruthy();
  });
});
