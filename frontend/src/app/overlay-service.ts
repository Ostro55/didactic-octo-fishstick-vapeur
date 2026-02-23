import { Injectable } from '@angular/core';
import {PhotoSmall} from "../PhotoModel";
import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse} from "../PhotoURL";

@Injectable({
  providedIn: 'root',
})
export class OverlayService {

  public overlaybehavior =  new BehaviorSubject<FlickrPhotoResponse | undefined>(undefined);


  public display = "none";

  public date :string = "";
  public photo : PhotoSmall = new PhotoSmall();

  public show(photo:PhotoSmall,date:string){
    this.display = "block";
    this.photo = photo;
    this.date= date;

  }

  public hide(){
    this.display = "none";

  }
  
}
