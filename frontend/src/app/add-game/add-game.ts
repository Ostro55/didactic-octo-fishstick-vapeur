import {Component, inject} from '@angular/core';
import FlickrService from "../flickr-service";
import {GameRequest, Photo} from "../../PhotoModel";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Router} from "@angular/router";
import {HomeButton} from "../home-button/home-button";

@Component({
  selector: 'app-add-game',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HomeButton
  ],
  templateUrl: './add-game.html',
  styleUrl: './add-game.css',
})
export class AddGame {
  public api = inject(FlickrService)
  public name:string = ""
  public genres: string[] =[
    "action",
    "thriller",
    "singleplayer",
    "horror",
    "romance",
    "multiplayer"
  ];

  public    price:      number = 0;
  public genre:      string[] = [];
  public makingTime: string = Date.now().toLocaleString();

  constructor(
      private router: Router, private formBuilder: FormBuilder ) {
      this.theForm = new FormBuilder().group({
        name: [this.name],
        price: [this.price],
      });
  }

  theForm: FormGroup ;

  public onGenreChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.genre  = Array.from(select.selectedOptions).map(option => option.value);

  }


  public addgame(){
    let formValues = this.theForm.getRawValue();
    var body:GameRequest = new GameRequest(formValues.name,formValues.price,this.genre,new Date(Date.now()))
    console.log(body)
    this.api.add_game(body)
    this.router.navigateByUrl('/');
  }

}
