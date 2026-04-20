import {Component, inject, Input, input} from '@angular/core';
import FlickrService from "../flickr-service";
import {FormsModule} from "@angular/forms";
import { ActivatedRoute } from '@angular/router'
import {BehaviorSubject} from "rxjs";
import {PhotoSmall} from "../../PhotoModel";

@Component({
  selector: 'app-search-bar',
  imports: [
    FormsModule
  ],
  templateUrl: './search-bar.html',
  styleUrl: './search-bar.css',
})
export class SearchBar {
  protected api = inject(FlickrService);
  public genres: string[] =[
    "action",
    "thriller",
    "singleplayer",
    "horror",
    "romance",
    "multiplayer"
  ];
  public minprice :number = -1;
  public maxprice :number = 0;

  public genresselect :string[] = [];


  constructor(private activateRoute: ActivatedRoute){}

  @Input() imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);






  public text = "";
  public search()
  {
    if (this.text != "")
    {

      this.api.search(this.text,this.genresselect,this.minprice,this.maxprice,this.imagelistv2);
    }
  }

  ngOnInit()
  {
    this.activateRoute.queryParamMap.subscribe(a => {
      var t = a.get("search")
      if (t != null)
      {
        this.text = t.toString();
        this.search()
      }

    })
  }
}
