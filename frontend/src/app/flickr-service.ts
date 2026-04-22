import {ApplicationConfig, inject, Injectable, signal, Signal} from '@angular/core';
import {BehaviorSubject, firstValueFrom} from "rxjs";
import {HttpClient, HttpParams, provideHttpClient, withFetch} from "@angular/common/http";
import {scheduleReadableStreamLike} from "rxjs/internal/scheduled/scheduleReadableStreamLike";
import {ApiResponse, GameRequest, LoginUser, Photo, PhotoSmall, PhotosPage} from "../PhotoModel";
import {CreateUser, FlickrPhotoResponse, PhotoInfo, RecommendedGame, UsersResponse} from "../PhotoURL";
import {AddGame} from "./add-game/add-game";
import {Utilisateur} from "./utilisateur";


@Injectable({
  providedIn: 'root',
})

class FlickrService {

    url = "http://localhost:8080";

  private http = inject(HttpClient);

  api_key = "9bece75e0eea9e622a75fa8a0eb1e6a2";

  public size :[string,string][] = [["","url_sq,url_t,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o"],["petite","url_m,url_n"],["moyenne","url_z,url_c"],["grande","url_l"]];

  public page : number = 1;

  public lastcall  :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {};


  public search(value : string,genre:string[],minprice : number,maxprice: number,  imagelistv2 : BehaviorSubject<PhotoSmall[]>){


      //https://serpapi.com/search.json?q=Apple&engine=google_images&ijn=0
      //https://www.flickr.com/services/rest/?method=flickr.test.echo&name=value
      /*
          this.http.get('https://serpapi.com/search.json', {params: {q : value,engine:"google_images"} ,responseType: 'json'}, ).subscribe((buffer) => {
            console.log('The image is ' + buffer + ' bytes large');
          });
       */
      //var params :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {}
      var params :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {

      };
      if (genre.length != 0  )
      {
        params["genre"] = genre;
      }
      if (minprice != -1)
      {
          params["minPrice"] = minprice;
          params["maxPrice"] = maxprice;
      }
      if (value != "")
      {
          params["name"] = value
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
        console.log(params)
        var res = this.http.get<Photo[]>(
            '/games'
            , {
                params : params,
                responseType: 'json'
            }, ).subscribe((buffer) => {
            console.log(buffer);
            var imagelist: PhotoSmall[] = buffer.filter(a => a.status == "accepted").map(a => {
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
        });
    }

    public searchParams_request( imagelistv2 : BehaviorSubject<PhotoSmall[]>)
    {
        var res = this.http.get<Photo[]>(
            '/games'
            , {
                responseType: 'json'
            }, ).subscribe((buffer) => {
            console.log(buffer);
            var imagelist: PhotoSmall[] = buffer.filter(a => a.status != "accepted").map(a => {
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

            format:"json",
            nojsoncallback: 1,
          },
          responseType: 'json'
        }, ).subscribe((buffer) => {
      data.next(buffer);

    });
  }


    public add_game(game: GameRequest)
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



        var res = this.http.post(
            '/games',
            game,
            ).subscribe(() => {
                console.log("send img")
        });
    }



    public Login_User(data : BehaviorSubject<UsersResponse  | undefined>,login:Utilisateur)
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
        var loginreq = new LoginUser(login.email,login.email,login.password)
        var res = this.http.post<UsersResponse>(
            '/users/login',
            loginreq
        ).subscribe(data) ;
    }

    public List_user(data : BehaviorSubject<UsersResponse[]  | undefined>)
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

        var res = this.http.get<UsersResponse[]>(
            '/users' ).subscribe(data) ;
    }

    public async Create_user(email : string,password: string,isAdmin : boolean){
      var  b: CreateUser = {
          username: email,
          email: email,
          isAdmin: isAdmin,
          password: password
      }
      var t =   await firstValueFrom(this.http.post(
          '/users',
          b
      ))



    }

    public approve_game(id: number) {
        return this.http.put(`/games/${id}/accept`, {});
    }

    public decline_game(id: number) {
        return this.http.delete(`/games/${id}/rejected`);
    }

}

export default FlickrService
