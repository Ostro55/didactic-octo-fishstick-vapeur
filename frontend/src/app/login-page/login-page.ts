import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../auth-service";
import {Router} from "@angular/router";
import {NgClass} from "@angular/common";

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
  constructor(private authService: AuthService,
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
      this.router.navigateByUrl('/admin');

    }
  }
  get formControls() { return this.loginForm.controls; }
  seConnecter(){
    console.log(this.loginForm.value);
    this.isSubmitted = true;
    if(this.loginForm.invalid){
      return 1;
    }
    this.authService.seConnecter(this.loginForm.value);
    this.router.navigateByUrl('/admin');
    return 0
  }
}
