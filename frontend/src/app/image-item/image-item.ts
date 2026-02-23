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
        NgOptimizedImage,
        AsyncPipe,
    ],
  templateUrl: './image-item.html',
  styleUrl: './image-item.css',
})
export class ImageItem {

  @Input()  public id : string = "";
  @Input() public image: PhotoSmall = PhotoSmall.PhotoSmall2( "https://cdn.search.brave.com/serp/v3/_app/immutable/assets/brave-logo-dark.5D16vJCY.svg");

    public overlay = inject(OverlayService);

    public autheur: string = "test";
    public title:string = ""

    public date:string = "";



    protected api = inject(FlickrService);

  photoflicker : FlickrPhotoResponse | undefined = undefined;

  ngOnInit()
  {
      if (this.image.image != undefined)
      {
          this.api.get_photo(this.id,this.image.image);

          this.image.image.subscribe(a =>
          {
              if (a != undefined)
              {
                  var d = new Date(Number(a.photo.dateuploaded) * 1000)
                  this.date = d.getDate() + '/' + (d.getMonth()+1) + '/' + d.getFullYear();
                  this.autheur = a.photo.owner.username;
                  this.title = a.photo.title._content;
                  this.photoflicker = a;
              }

          })
      }

  }

  public showinfo()
  {
      this.overlay.overlaybehavior.next(this.photoflicker)
      this.overlay.show(this.image,this.date)
  }


    protected readonly Date = Date;
}
