import { Component } from '@angular/core';
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {HomeButton} from "../home-button/home-button";

@Component({
  selector: 'app-admin-page',
  imports: [
    HomeButton
  ],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.css',
})
export class AdminPage {

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit() {
  }

  seDeconnecter(){
    this.authService.SeDeconnecter();
    this.router.navigateByUrl('/connexion');
  }
}
