import {Component, inject} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../auth-service";
import FlickrService from "../flickr-service";
import {Router} from "@angular/router";
import {NgClass} from "@angular/common";

@Component({
  selector: 'app-sigin-page',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './sigin-page.html',
  styleUrl: './sigin-page.css',
})
export class SiginPage {
  loginForm!: FormGroup;
  isSubmitted  =  false;
  public authService: AuthService = inject(AuthService)
  public api = inject(FlickrService)

  constructor(
      private router: Router, private formBuilder: FormBuilder ) { }

  ngOnInit() {
    const x  =  this.formBuilder.group({
      email: ['', Validators.required],
      password: ['', Validators.required]
    });
    if (x.value != null)
    {
      this.loginForm = x;
    }
    if (this.authService.estConnecte())
    {
      console.log(this.authService.userInfo)

      if (this.authService.userInfo?.isAdmin )
      {
        this.router.navigateByUrl('/admin');

      }
      else {
        this.router.navigateByUrl('/user');

      }
    }
  }
  get formControls() { return this.loginForm.controls; }


  async Singup() {
    console.log(this.loginForm.value);
    await this.api.Create_user(this.loginForm.value['email'],this.loginForm.value['password'],false)
    await this.router.navigateByUrl('/');

  }
}
