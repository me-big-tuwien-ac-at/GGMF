import { TestBed } from '@angular/core/testing';

import { ModularisationDataService } from './modularisation-data.service';

describe('ModularisationDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ModularisationDataService = TestBed.get(ModularisationDataService);
    expect(service).toBeTruthy();
  });
});
