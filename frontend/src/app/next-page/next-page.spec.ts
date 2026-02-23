import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NextPage } from './next-page';

describe('NextPage', () => {
  let component: NextPage;
  let fixture: ComponentFixture<NextPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NextPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NextPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
