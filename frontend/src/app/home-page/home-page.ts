import {Component, inject} from '@angular/core';
import {HomeButton} from "../home-button/home-button";
import {Overlay} from "../overlay/overlay";
import {SearchBar} from "../search-bar/search-bar";
import {ImageList} from "../image-list/image-list";
import {NextPage} from "../next-page/next-page";
import {BehaviorSubject} from "rxjs";
import {PhotoSmall} from "../../PhotoModel";
import {FlickrService} from "../flickr-service";
import {AuthService} from "../auth-service";

@Component({
  selector: 'app-home-page',
  imports: [
    HomeButton,
    ImageList,
    Overlay,
  ],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePage {

  protected api = inject(FlickrService);
  public authService: AuthService = inject(AuthService)

  imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);

  ngOnInit()
  {
    this.api.search("the best","","","",0,false,false,"",this.imagelistv2)
  }
}
