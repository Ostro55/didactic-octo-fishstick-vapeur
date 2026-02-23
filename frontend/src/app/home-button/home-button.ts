import { Component } from '@angular/core';
import {NgOptimizedImage} from "@angular/common";
import {ReactiveFormsModule} from "@angular/forms";
import {form} from "@angular/forms/signals";

@Component({
  selector: 'app-home-button',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './home-button.html',
  styleUrl: './home-button.css',
})
export class HomeButton {

  protected readonly form = form;
}
