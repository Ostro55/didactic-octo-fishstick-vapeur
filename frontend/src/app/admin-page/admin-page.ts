import {Component, inject} from '@angular/core';
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {HomeButton} from "../home-button/home-button";
import {AuthGuard} from "../auth-guard";
import FlickrService from "../flickr-service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {form} from "@angular/forms/signals";

@Component({
  selector: 'app-admin-page',
  imports: [
    HomeButton,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.css',
})
export class AdminPage {
  public authService: AuthService = inject(AuthService)

  constructor( private router: Router) { }
  protected guard = inject(AuthGuard);


  ngOnInit() {
  }

  seDeconnecter(){
    this.authService.SeDeconnecter();
    this.router.navigateByUrl('/connexion');
  }

  ban(userid : string)
  {
    console.log("ban user with id: " + userid);

  }

  unban(userid : string)
  {
    console.log("unban user with id: " + userid);

  }




  protected readonly form = form;
}
