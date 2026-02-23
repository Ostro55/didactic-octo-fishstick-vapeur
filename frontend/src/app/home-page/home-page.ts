import { Component } from '@angular/core';
import {HomeButton} from "../home-button/home-button";
import {Overlay} from "../overlay/overlay";
import {SearchBar} from "../search-bar/search-bar";
import {ImageList} from "../image-list/image-list";
import {NextPage} from "../next-page/next-page";

@Component({
  selector: 'app-home-page',
  imports: [
    HomeButton,
  ],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePage {

}
