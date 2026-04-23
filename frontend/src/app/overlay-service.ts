import { Injectable } from '@angular/core';
import {Game, GameSmall} from "../GameModel";
import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse} from "../GameURL";

@Injectable({
  providedIn: 'root',
})
export class OverlayService {

  public overlaybehavior =  new BehaviorSubject<GameSmall | undefined>(undefined);


  public display = "none";

  public date :string = "";
  public photo : GameSmall = new GameSmall();

  public show(photo:GameSmall, date:string){
    this.display = "block";
    this.photo = photo;
    this.date= date;

  }

  public hide(){
    this.display = "none";

  }
  
}
