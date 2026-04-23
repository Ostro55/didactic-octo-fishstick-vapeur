import {Component, inject} from '@angular/core';
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {HomeButton} from "../home-button/home-button";
import {AuthGuard} from "../auth-guard";
import FlickrService from "../flickr-service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {form} from "@angular/forms/signals";
import {GameSmall} from "../../GameModel";
import {BehaviorSubject} from "rxjs";
import {AsyncPipe} from "@angular/common";
import {UsersResponse} from "../../GameURL";

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
  //this part work perfectly
  public authService: AuthService = inject(AuthService)

  constructor( private router: Router) { }
  protected guard = inject(AuthGuard);
  protected api = inject(FlickrService);
  imagelistv2 : BehaviorSubject<GameSmall[]> = new BehaviorSubject<GameSmall[]>([]);
  listUser : BehaviorSubject<UsersResponse[] | undefined> = new BehaviorSubject<UsersResponse[] | undefined>([]);
  selectedGame: GameSmall | null = null;
  showOverlay = false;

  ngOnInit() {
    this.api.searchParams_request(this.imagelistv2)
    this.listUser.subscribe(value => {console.log(value) ;
    console.log("this was the list of user")})

  }

  seDeconnecter(){
    this.authService.SeDeconnecter();
    this.router.navigateByUrl('/connexion');
  }

  ban(userid : string )
  {
    console.log(userid)
    this.api.ban_user(userid)
  }

  found(userid : string )
  {
    console.log(userid)
    this.api.List_user(this.listUser)
  }

  openOverlay(game: GameSmall) {
    this.selectedGame = game;
    this.showOverlay = true;
  }

  closeOverlay() {
    this.selectedGame = null;
    this.showOverlay = false;
  }

  approveGame() {
    if (this.selectedGame) {
      this.api.approve_game(this.selectedGame.id).subscribe(() => {
        this.api.searchParams_request(this.imagelistv2);
        this.closeOverlay();
      });
    }
  }

  declineGame() {
    if (this.selectedGame) {
      this.api.decline_game(this.selectedGame.id).subscribe(() => {
        this.api.searchParams_request(this.imagelistv2);
        this.closeOverlay();
      });

    }
  }

  protected readonly form = form;
  protected userid: string = "";
  protected username: string = "";
}
