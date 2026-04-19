import {inject, Injectable} from '@angular/core';
import {UserConnectd, Utilisateur} from "./utilisateur";
import FlickrService from './flickr-service'
import {BehaviorSubject} from "rxjs";
import {UsersResponse} from "../PhotoURL";

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor() { }
  api = inject(FlickrService)

  public userInfo: UserConnectd | null = null;

  //add request here to connect
  public seConnecter(userInfo: Utilisateur){
    var v = new BehaviorSubject<UsersResponse[]  | undefined>(undefined)
    this.api.List_user(v)

    return v.subscribe((a) =>
    {
      if (a != undefined)
      {
        for (var t of a) {
            if (userInfo.email == t.email){
              localStorage.setItem('User', userInfo.email);
              localStorage.setItem('ID', String(t.id));
              localStorage.setItem('Admin', String(t.isAdmin));


              this.userInfo = new UserConnectd(userInfo.email,String(t.id),t.isAdmin)
              console.log(this.userInfo)
            }
        }

      }

    })


  }
  public estConnecte(){
    if (localStorage.getItem('ID') !== null){
      this.Update()
      return true
    }
    return false

  }

  public Update()
  {
    if (localStorage.getItem('User') != null && localStorage.getItem('ID') != null && localStorage.getItem('Admin')!= null)
    {
      this.userInfo = new UserConnectd(localStorage.getItem('User')!,localStorage.getItem('ID')!,localStorage.getItem('Admin')=='true')
    }

  }

  public SeDeconnecter(){
    localStorage.removeItem('User');
    localStorage.removeItem('ID');
    localStorage.removeItem('Admin');
    this.userInfo = null
  }
}
