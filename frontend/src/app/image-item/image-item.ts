import {Component, inject, Input} from '@angular/core';
import {AsyncPipe, NgOptimizedImage} from "@angular/common";
import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse, PhotoInfo} from "../../GameURL";
import FlickrService from "../flickr-service";
import {Game, GameSmall} from "../../GameModel";
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
  @Input() public image: GameSmall = GameSmall.GameSmall2();
    //this part work perfectly

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
                  this.photoflicker = a;
              }

          })
      }

  }

  public showinfo()
  {
      this.overlay.overlaybehavior.next(this.image)
      this.overlay.show(this.image,this.image.release_date)
  }


    protected readonly Date = Date;
}
