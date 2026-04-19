import {Component, inject, Input} from '@angular/core';
import FlickrService from "../flickr-service";
import {ImageItem} from "../image-item/image-item";
import {Photo, PhotoSmall} from "../../PhotoModel";
import {AsyncPipe} from "@angular/common";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'app-image-list',
  imports: [
    ImageItem,
    AsyncPipe
  ],
  templateUrl: './image-list.html',
  styleUrl: './image-list.css',
})
export class ImageList {
  @Input() imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);

  protected api = inject(FlickrService);

  public v = 0;

  public v2 = 0;


  public list: PhotoSmall[] = [];

  ngOnInit()
  {
    this.imagelistv2.subscribe(value => {
      this.v += 1;
      //console.log(this.v);

      this.list = value;
      //console.log("updated");
      //console.log(this.list);

      if (this.list.length> 0)
      {
        //console.log(this.list[0]);
      }

    })
  }





}
