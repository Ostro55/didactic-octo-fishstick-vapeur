import {Component, inject, Input, input} from '@angular/core';
import {FlickrService} from "../flickr-service";
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
  public mindate :string = "";
  public maxdate :string = "";

  public sizeselect :string = "";

  public safeselect :number = -1;
  public safe :[string,number][] = [["",-1],["safe",0],["moderate",1],["restricted",2]];

  public is_ingallery : boolean = false;

  public is_hasgeo : boolean = false;

  public texttag :string = "";

  public sortselect :string = "";
  public sort :string[] = ["","date-posted-asc","date-posted-desc","date-taken-asc","date-taken-desc","interestingness-desc","interestingness-asc","relevance"];

  constructor(private activateRoute: ActivatedRoute){}

  @Input() imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);






  public text = "";
  public search()
  {
    if (this.text != "")
    {

      this.api.search(this.text,this.mindate,this.maxdate,this.sortselect,this.safeselect,this.is_hasgeo,this.is_ingallery,this.texttag,this.imagelistv2);
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
