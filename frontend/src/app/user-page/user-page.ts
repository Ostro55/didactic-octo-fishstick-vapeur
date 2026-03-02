import { Component } from '@angular/core';
import {HomeButton} from "../home-button/home-button";

@Component({
  selector: 'app-user-page',
  imports: [
    HomeButton
  ],
  templateUrl: './user-page.html',
  styleUrl: './user-page.css',
})
export class UserPage {

}
