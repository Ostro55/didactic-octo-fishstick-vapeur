import {Component, inject} from '@angular/core';
import {OverlayService} from "../overlay-service";
import {AsyncPipe, NgOptimizedImage} from "@angular/common";
import {BehaviorSubject} from "rxjs";
import {Photo, PhotoSmall} from "../../PhotoModel";
import {FlickrPhotoResponse} from "../../PhotoURL";

@Component({
  selector: 'app-overlay',
  imports: [
    AsyncPipe
  ],
  templateUrl: './overlay.html',
  styleUrl: './overlay.css',
})
export class Overlay {

  public overlay = inject(OverlayService);


  public autheur: string = "test";
  public title:string = ""

  public date:string = "";

  public description:string = "";


  public location: string  = "";
  public lat: string = "";

  ngOnInit()
  {
    this.overlay.overlaybehavior.subscribe(a => {
      if (a != undefined)
      {
        console.log()
        this.autheur = a.photo.owner.username;
        this.title = a.photo.title._content;
        this.description = a.photo.description._content;
        if (a.photo.location !=undefined)
        {
          this.location = a.photo.location.country._content + "," + a.photo.location.region._content + "," + a.photo.location.locality._content;
          this.lat = "lat: " + a.photo.location.latitude + ", long" + a.photo.location.longitude;
        }
        else {
          this.location = "";
          this.lat = "";
        }
      }
    })
  }

}
