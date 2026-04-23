import {ApplicationConfig, inject, Injectable, signal, Signal} from '@angular/core';
import {BehaviorSubject, firstValueFrom} from "rxjs";
import {HttpClient, HttpParams, provideHttpClient, withFetch} from "@angular/common/http";
import {scheduleReadableStreamLike} from "rxjs/internal/scheduled/scheduleReadableStreamLike";
import {ApiResponse, GameRequest, LoginUser, Game, GameSmall, GamePage} from "../GameModel";
import {CreateUser, FlickrPhotoResponse, PhotoInfo, RecommendedGame, UsersResponse} from "../GameURL";
import {AddGame} from "./add-game/add-game";
import {Utilisateur} from "./utilisateur";


@Injectable({
  providedIn: 'root',
})

class FlickrService {
  private http = inject(HttpClient);

  public size :[string,string][] = [["","url_sq,url_t,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o"],["petite","url_m,url_n"],["moyenne","url_z,url_c"],["grande","url_l"]];

  public page : number = 1;

  public lastcall  :  Record<string, string | number | boolean | readonly (string | number | boolean)[]> = {};


  public search(value : string,genre:string[],minprice : number,maxprice: number,  imagelistv2 : BehaviorSubject<GameSmall[]>){
      //this part work perfectly

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

    public searchChangePage(imagelistv2 : BehaviorSubject<GameSmall[]>)
    {
        this.lastcall["page"] = this.page;
        this.searchParams(this.lastcall,imagelistv2);
    }


    public searchParams(params : Record<string, string | number | boolean | readonly (string | number | boolean)[]>, imagelistv2 : BehaviorSubject<GameSmall[]>)
    {
        console.log(params)
        var res = this.http.get<Game[]>(
            '/games'
            , {
                params : params,
                responseType: 'json'
            }, ).subscribe((buffer) => {
            console.log(buffer);
            var imagelist: GameSmall[] = buffer.filter(a => a.status == "accepted").map(a => {
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

    public searchParams_request( imagelistv2 : BehaviorSubject<GameSmall[]>)
    {
        var res = this.http.get<Game[]>(
            '/games'
            , {
                responseType: 'json'
            }, ).subscribe((buffer) => {
            console.log(buffer);
            var imagelist: GameSmall[] = buffer.filter(a => a.status != "accepted").map(a => {
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



    public find_url(photo :Game)
  {
    var v =  new GameSmall();

    return v;
  }

  public get_game(id : number, data : BehaviorSubject<FlickrPhotoResponse  | undefined>)
  {
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

        var res = this.http.post(
            '/games',
            game,
            ).subscribe(() => {
                console.log("send img")
        });
    }


    public ban_user(id:string)
    {


        var res = this.http.delete(
            '/users/' + id
        ).subscribe(() => {
            console.log("ban user")
        });
    }



    public Login_User(data : BehaviorSubject<UsersResponse  | undefined>,login:Utilisateur)
    {
         var loginreq = new LoginUser(login.email,login.email,login.password)
        var res = this.http.post<UsersResponse>(
            '/users/login',
            loginreq
        ).subscribe(data) ;
    }

    public List_user(data : BehaviorSubject<UsersResponse[]  | undefined>)
    {

        var res = this.http.get<UsersResponse[]>(
            '/users' ).subscribe(data.next.bind(data)) ;
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
