import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchBar } from './search-bar';

describe('SearchBar', () => {
  let component: SearchBar;
  let fixture: ComponentFixture<SearchBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchBar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SearchBar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('check search bar is here',() => {
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector("input");
    expect(input).not.toBeNull();
  })

  it('check label bar is here',() => {
    fixture.detectChanges();
    const label = fixture.nativeElement.querySelector("label");
    expect(label).not.toBeNull();
  })
});
