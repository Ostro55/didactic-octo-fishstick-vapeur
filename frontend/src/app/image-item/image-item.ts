import {Component, inject, Input} from '@angular/core';
import {AsyncPipe, NgOptimizedImage} from "@angular/common";
import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse, PhotoInfo} from "../../PhotoURL";
import {FlickrService} from "../flickr-service";
import {Photo, PhotoSmall} from "../../PhotoModel";
import {OverlayService} from "../overlay-service";

@Component({
  selector: 'app-image-item',
    imports: [
    ],
  templateUrl: './image-item.html',
  styleUrl: './image-item.css',
})
export class ImageItem {

  @Input()  public id : number = 0;
  @Input() public image: PhotoSmall = PhotoSmall.PhotoSmall2();

    public overlay = inject(OverlayService);



    protected api = inject(FlickrService);

  photoflicker : FlickrPhotoResponse | undefined = undefined;

  ngOnInit()
  {
      if (this.image.id != -1)
      {
          this.api.get_game(this.id,this.image.image);

          this.image.image.subscribe(a =>
          {
              if (a != undefined)
              {
                  var d = new Date(Number(a.photo.dateuploaded) * 1000)
                  this.photoflicker = a;
              }

          })
      }

  }

  public showinfo()
  {
      this.overlay.overlaybehavior.next(this.photoflicker)
      this.overlay.show(this.image,this.image.release_date)
  }


    protected readonly Date = Date;
}
