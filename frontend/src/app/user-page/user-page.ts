import {Component, inject} from '@angular/core';
import {HomeButton} from "../home-button/home-button";
import {AuthService} from "../auth-service";
import {FormsModule} from "@angular/forms";
import {Router} from "@angular/router";
import {AuthGuard} from "../auth-guard";

@Component({
  selector: 'app-user-page',
  imports: [
    HomeButton,
    FormsModule
  ],
  templateUrl: './user-page.html',
  styleUrl: './user-page.css',
})
export class UserPage {
  public authService: AuthService = inject(AuthService)

  constructor( private router: Router) { }
  protected guard = inject(AuthGuard);


  ngOnInit() {
  }

  seDeconnecter(){
    this.authService.SeDeconnecter();
    this.router.navigateByUrl('/connexion');
  }
}
