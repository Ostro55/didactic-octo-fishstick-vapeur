import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddGame } from './add-game';

describe('AddGame', () => {
  let component: AddGame;
  let fixture: ComponentFixture<AddGame>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddGame]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddGame);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
