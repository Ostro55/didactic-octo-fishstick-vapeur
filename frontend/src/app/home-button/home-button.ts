import { Component } from '@angular/core';
import {ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'app-home-button',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './home-button.html',
  styleUrl: './home-button.css',
})
export class HomeButton {
}
