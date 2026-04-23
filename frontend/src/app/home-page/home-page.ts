import {Component, inject} from '@angular/core';
import {HomeButton} from "../home-button/home-button";
import {Overlay} from "../overlay/overlay";
import {SearchBar} from "../search-bar/search-bar";
import {ImageList} from "../image-list/image-list";
import {NextPage} from "../next-page/next-page";
import {BehaviorSubject} from "rxjs";
import {GameSmall} from "../../GameModel";
import FlickrService from "../flickr-service";
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {FormBuilder} from "@angular/forms";

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

  imagelistv2 : BehaviorSubject<GameSmall[]> = new BehaviorSubject<GameSmall[]>([]);
  constructor(
      private router: Router, private formBuilder: FormBuilder ) { }

  ngOnInit()
  {
    this.api.search("",[],-1,0,this.imagelistv2)
  }

  public ButtonAddgame()
  {
    this.router.navigateByUrl('/addgame');

  }
}
