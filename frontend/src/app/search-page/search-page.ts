import { Component } from '@angular/core';
import {ImageList} from "../image-list/image-list";
import {NextPage} from "../next-page/next-page";
import {Overlay} from "../overlay/overlay";
import {SearchBar} from "../search-bar/search-bar";

@Component({
  selector: 'app-search-page',
    imports: [
        ImageList,
        NextPage,
        Overlay,
        SearchBar
    ],
  templateUrl: './search-page.html',
  styleUrl: './search-page.css',
})
export class SearchPage {


}
