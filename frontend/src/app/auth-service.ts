import { Injectable } from '@angular/core';
import {UserConnectd, Utilisateur} from "./utilisateur";

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor() { }

  public userInfo: UserConnectd | null = null;

  //add request here to connect
  public seConnecter(userInfo: Utilisateur){
    localStorage.setItem('ACCESS_TOKEN', "access_token");
    this.userInfo = new UserConnectd(userInfo.email,"0",true)
    console.log(this.userInfo)
  }
  public estConnecte(){
    return localStorage.getItem('ACCESS_TOKEN') !== null;

  }
  public SeDeconnecter(){
    localStorage.removeItem('ACCESS_TOKEN');
    this.userInfo = null
  }
}
