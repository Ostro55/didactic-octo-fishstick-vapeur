import {Component, inject} from '@angular/core';
import {FlickrService} from "../flickr-service";
import {AsyncPipe} from "@angular/common";

@Component({
  selector: 'app-next-page',
  imports: [
    AsyncPipe
  ],
  templateUrl: './next-page.html',
  styleUrl: './next-page.css',
})
export class NextPage {
  protected api = inject(FlickrService);

  public next()
  {
    this.api.page += 1;
    this.api.searchChangePage();
  }

  public previous()
  {
    this.api.page -= 1;
    this.api.searchChangePage();

  }

}
