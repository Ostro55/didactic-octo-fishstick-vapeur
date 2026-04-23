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


  public autheur: string  | null | undefined = "test";
  public title:string  | null | undefined = ""
  public description:string | null | undefined = "";
  public img_url: string | null | undefined = "";
  public genre: string[] = [];
  public realease_date: string | null | undefined = "";
  public price: number = 0;

  ngOnInit()
  {
    this.overlay.overlaybehavior.subscribe(a => {
      if (a != undefined)
      {
        console.log(a)
        this.autheur = a.editor;
        this.title = a.name;
        this.description = a.description;
        this.img_url = a.img_url;
        this.genre = a.genre;
        this.realease_date = a.release_date;
        this.price = a.price;
      }
    })
  }

}
