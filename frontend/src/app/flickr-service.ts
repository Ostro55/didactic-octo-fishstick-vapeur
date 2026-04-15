import {ApplicationConfig, inject, Injectable, signal, Signal} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {HttpClient, HttpParams, provideHttpClient, withFetch} from "@angular/common/http";
import {scheduleReadableStreamLike} from "rxjs/internal/scheduled/scheduleReadableStreamLike";
import {ApiResponse, Photo, PhotoSmall, PhotosPage} from "../PhotoModel";
import {FlickrPhotoResponse, PhotoInfo} from "../PhotoURL";


@Injectable({
  providedIn: 'root',
})

export class FlickrService {

    url = "http://localhost:8080";

  private http = inject(HttpClient);

  api_key = "9bece75e0eea9e622a75fa8a0eb1e6a2";

  public size :[string,string][] = [["","url_sq,url_t,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o"],["petite","url_m,url_n"],["moyenne","url_z,url_c"],["grande","url_l"]];

  public page : number = 1;

  public lastcall  :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {};


  public search(value : string,mindate :string, maxdate:string,sort: string,safe : number, geo:boolean,in_gallery : boolean,tags : string,
                imagelistv2 : BehaviorSubject<PhotoSmall[]>){


      //https://serpapi.com/search.json?q=Apple&engine=google_images&ijn=0
      //https://www.flickr.com/services/rest/?method=flickr.test.echo&name=value
      /*
          this.http.get('https://serpapi.com/search.json', {params: {q : value,engine:"google_images"} ,responseType: 'json'}, ).subscribe((buffer) => {
            console.log('The image is ' + buffer + ' bytes large');
          });
       */
      var params :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {}
      /*
      var params :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {
        api_key: this.api_key,

        method : "flickr.photos.search",
        format:"json",
        nojsoncallback: 1,
        text : value,
        extras :"url_sq,url_t,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o",
        page : this.page,

      };
      if (mindate != "" && maxdate != "")
      {
        params["min_upload_date"] = mindate;
        params["max_upload_date"] = maxdate;
      }
      if (sort != "")
      {
        params["sort"] = sort; //date-posted-asc, date-posted-desc, date-taken-asc, date-taken-desc, interestingness-desc, interestingness-asc, and relevance
      }
      if (safe != -1)
      {
        params["safe_search"] = safe;
      }
      if (geo )
      {
        params["has_geo"] = geo;
      }
      if (in_gallery)
      {
        params["in_gallery"] = in_gallery;
      }
      if (tags != "")
      {
          params["tags"] = tags;
      }
      */
      this.lastcall = params;

      this.searchParams(params,imagelistv2);
  }

    public searchChangePage(imagelistv2 : BehaviorSubject<PhotoSmall[]>)
    {
        this.lastcall["page"] = this.page;
        this.searchParams(this.lastcall,imagelistv2);
    }


    public searchParams(params : Record<string, string | number | boolean | readonly (string | number | boolean)[]>, imagelistv2 : BehaviorSubject<PhotoSmall[]>)
    {
        var res = this.http.get<Photo[]>(
            '/games'
            , {
                params : params,
                responseType: 'json'
            }, ).subscribe({
            next: (buffer) => {
            console.log(buffer);
            var imagelist: PhotoSmall[] = buffer.map(a => {
                var v =  this.find_url(a);

                v.name = a.name;
                v.id = a.id;
                v.genre = a.genre;
                v.price = a.price;
                v.description = a.description == null ? "" : a.description;
                v.editor = a.editor == null ? "" : a.editor;
                v.img_url = a.img_url == null ? "" : a.img_url;

                return v;
            });
            console.log(imagelist);

            //this.imagelistv2.next(imagelist);
            imagelistv2.next(imagelist);
            },
            error: (err) => {
                console.error('Failed to load games:', err);
            }
        });
    }

  public find_url(photo :Photo)
  {
    var v =  new PhotoSmall();

    return v;
  }

  public get_game(id : number, data : BehaviorSubject<FlickrPhotoResponse  | undefined>)
  {
    //https://www.flickr.com/services/rest/?
    // method=flickr.photos.getInfo&
    // api_key=a9ea63761e96e6b316d83d062cce5726&
    // photo_id=54993765443&
    // format=json&
    // nojsoncallback=1&
    // auth_token=72157720960825727-a8bc48f55acbafc8&
    // api_sig=89dc8eee7392a8133e416994a773f9f0
    //https://www.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=e3187ef450a7c5c2a9429c10f06297d4&photo_id=54993765443&format=json&nojsoncallback=1
    var res = this.http.get<FlickrPhotoResponse>(
        '/games/' + id
        , {
          params : {

            method : "flickr.photos.getInfo",
            format:"json",
            nojsoncallback: 1,
          },
          responseType: 'json'
        }, ).subscribe({
      next: (buffer) => {
        data.next(buffer);
      },
      error: (err) => {
        console.error('Failed to load game details:', err);
      }
    });
  }

}
