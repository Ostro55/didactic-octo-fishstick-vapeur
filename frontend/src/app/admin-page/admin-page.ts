import {Component, inject} from '@angular/core';
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {HomeButton} from "../home-button/home-button";
import {AuthGuard} from "../auth-guard";
import FlickrService from "../flickr-service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {form} from "@angular/forms/signals";
import {PhotoSmall} from "../../PhotoModel";
import {BehaviorSubject} from "rxjs";
import {AsyncPipe} from "@angular/common";

@Component({
  selector: 'app-admin-page',
  imports: [
    HomeButton,
    ReactiveFormsModule,
    FormsModule,
    AsyncPipe
  ],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.css',
})
export class AdminPage {
  public authService: AuthService = inject(AuthService)

  constructor( private router: Router) { }
  protected guard = inject(AuthGuard);
  protected api = inject(FlickrService);
  imagelistv2 : BehaviorSubject<PhotoSmall[]> = new BehaviorSubject<PhotoSmall[]>([]);

  selectedGame: PhotoSmall | null = null;
  showOverlay = false;

  ngOnInit() {
    this.api.searchParams_request(this.imagelistv2)

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

  openOverlay(game: PhotoSmall) {
    this.selectedGame = game;
    this.showOverlay = true;
  }

  closeOverlay() {
    this.selectedGame = null;
    this.showOverlay = false;
  }

  approveGame() {
    if (this.selectedGame) {
      /*this.api.approve_game(this.selectedGame.id).subscribe(() => {
        this.api.searchParams_request(this.imagelistv2);
        this.closeOverlay();
      }); */
    }
  }

  declineGame() {
    if (this.selectedGame) {
      /*this.api.decline_game(this.selectedGame.id).subscribe(() => {
        this.api.searchParams_request(this.imagelistv2);
        this.closeOverlay();
      }); */
    }
  }

  protected readonly form = form;
}
