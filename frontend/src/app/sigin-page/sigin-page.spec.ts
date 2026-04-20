import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SiginPage } from './sigin-page';

describe('SiginPage', () => {
  let component: SiginPage;
  let fixture: ComponentFixture<SiginPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SiginPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SiginPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
