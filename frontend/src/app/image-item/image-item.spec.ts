import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageItem } from './image-item';
import {GameSmall} from "../../GameModel";

describe('ImageItem', () => {
  let component: ImageItem;
  let fixture: ComponentFixture<ImageItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImageItem]
    })
    .compileComponents();
    fixture = TestBed.createComponent(ImageItem);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {

    fixture.detectChanges();

    expect(fixture).toBeTruthy();
  });


});
