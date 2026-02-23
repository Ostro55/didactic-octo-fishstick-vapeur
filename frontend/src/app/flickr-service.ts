import {ApplicationConfig, inject, Injectable, signal, Signal} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {HttpClient, HttpParams, provideHttpClient, withFetch} from "@angular/common/http";
import {scheduleReadableStreamLike} from "rxjs/internal/scheduled/scheduleReadableStreamLike";
import {ApiResponse, Photo, PhotoSmall} from "../PhotoModel";
import {FlickrPhotoResponse, PhotoInfo} from "../PhotoURL";


@Injectable({
  providedIn: 'root',
})

export class FlickrService {


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
        var res = this.http.get<ApiResponse>(
            'https://www.flickr.com/services/rest/'
            , {
                params : params,
                responseType: 'json'
            }, ).subscribe((buffer) => {
            console.log(buffer);
            var imagelist: PhotoSmall[] = buffer.photos.photo.map(a => {
                var v =  this.find_url(a);

                v.title = a.title;
                v.id = a.id;

                return v;
            });
            console.log(imagelist);

            //this.imagelistv2.next(imagelist);
            imagelistv2.next(imagelist);
        });
    }

  public find_url(photo :Photo)
  {
    var v =  new PhotoSmall();

    let l = ["sq", "t", "s", "q", ,"o"];
    for (var i of l)
    {
      // @ts-ignore
      if(photo["url_"+i] != undefined)
      {
        // @ts-ignore
        v.url = photo["url_"+i];
        // @ts-ignore
        v.height = photo["height_" +i];
        // @ts-ignore
        v.width = photo["width_" + i];
      }
      v.urlthumb =photo["url_t"];
      v.heightthumb = photo["height_t"];
      v.widththumb = photo["width_t"];

    }

    return v;
  }

  public get_photo(id : string, data : BehaviorSubject<FlickrPhotoResponse  | undefined>)
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
        'https://www.flickr.com/services/rest/'
        , {
          params : {
            api_key: this.api_key,

            method : "flickr.photos.getInfo",
            format:"json",
            nojsoncallback: 1,
            photo_id : id,
          },
          responseType: 'json'
        }, ).subscribe((buffer) => {
      data.next(buffer);

    });
  }

}
