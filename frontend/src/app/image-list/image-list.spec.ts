import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageList } from './image-list';

describe('ImageList', () => {
  let component: ImageList;
  let fixture: ComponentFixture<ImageList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImageList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ImageList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
