import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModularisationComponent } from './modularisation.component';

describe('ModularisationComponent', () => {
  let component: ModularisationComponent;
  let fixture: ComponentFixture<ModularisationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModularisationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModularisationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
