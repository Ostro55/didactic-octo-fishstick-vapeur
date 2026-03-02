import {Component, inject} from '@angular/core';
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {HomeButton} from "../home-button/home-button";
import {AuthGuard} from "../auth-guard";
import {FlickrService} from "../flickr-service";

@Component({
  selector: 'app-admin-page',
  imports: [
    HomeButton
  ],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.css',
})
export class AdminPage {

  constructor(public authService: AuthService, private router: Router) { }
  protected guard = inject(AuthGuard);


  ngOnInit() {
  }

  seDeconnecter(){
    this.authService.SeDeconnecter();
    this.router.navigateByUrl('/connexion');
  }
}
