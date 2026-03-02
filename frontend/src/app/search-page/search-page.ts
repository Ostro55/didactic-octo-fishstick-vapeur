import {Component, Input} from '@angular/core';
import {ImageList} from "../image-list/image-list";
import {NextPage} from "../next-page/next-page";
import {Overlay} from "../overlay/overlay";
import {SearchBar} from "../search-bar/search-bar";
import {BehaviorSubject} from "rxjs";
import {PhotoSmall} from "../../PhotoModel";
import {HomeButton} from "../home-button/home-button";

@Component({
  selector: 'app-search-page',
    imports: [
        ImageList,
        NextPage,
        Overlay,
        SearchBar,
        HomeButton
    ],
  templateUrl: './search-page.html',
  styleUrl: './search-page.css',
})
export class SearchPage {
    imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);

NgOnInit()
{
    console.log("test")
}
}
