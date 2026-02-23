import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {SearchBar} from "./search-bar/search-bar";
import {ImageList} from "./image-list/image-list";
import {Overlay} from "./overlay/overlay";
import {NextPage} from "./next-page/next-page";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SearchBar, ImageList, Overlay, NextPage],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('photo-search');
}
