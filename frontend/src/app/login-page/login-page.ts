import {Component, inject} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {NgClass} from "@angular/common";
import {FlickrService} from "../flickr-service";

@Component({
  selector: 'app-login-page',
  imports: [
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
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

  async seConnecter(){
    console.log(this.loginForm.value);
    this.isSubmitted = true;
    if(this.loginForm.invalid){
      return 1;
    }
    this.authService.seConnecter(this.loginForm.value);
    await new Promise(f => setTimeout(f, 1000));

    console.log(this.authService.userInfo);

    if (this.authService.userInfo != null)
    {
      if (this.authService.userInfo.isAdmin)
      {
        this.router.navigateByUrl('/admin');

      }
      else {
        this.router.navigateByUrl('/user-page');

      }
    }
    this.router.navigateByUrl('/admin');
    return 0
  }

  async Singup() {
    console.log(this.loginForm.value);
    await this.api.Create_user(this.loginForm.value['email'],this.loginForm.value['password'],true)
    await this.router.navigateByUrl('/');

  }
}
