import {Component, inject, Input} from '@angular/core';
import {FlickrService} from "../flickr-service";
import {AsyncPipe} from "@angular/common";
import {BehaviorSubject} from "rxjs";
import {PhotoSmall} from "../../PhotoModel";

@Component({
  selector: 'app-next-page',
  imports: [
    AsyncPipe
  ],
  templateUrl: './next-page.html',
  styleUrl: './next-page.css',
})
export class NextPage {
  @Input() imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);

  protected api = inject(FlickrService);

  public next()
  {
    this.api.page += 1;
    this.api.searchChangePage(this.imagelistv2);
  }

  public previous()
  {
    this.api.page -= 1;
    this.api.searchChangePage(this.imagelistv2);

  }

}
