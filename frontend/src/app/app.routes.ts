import { Routes } from '@angular/router';
import {UserPage} from "./user-page/user-page";
import {HomePage} from "./home-page/home-page";
import {SearchBar} from "./search-bar/search-bar";
import {SearchPage} from "./search-page/search-page";


export const routes: Routes = [

    { path: '', component: HomePage },
    { path: 'user', component: UserPage},
    { path: 'search', component: SearchPage},

    //{ path: 'login', component: LoginComponent },
    //{ path: 'register', component: RegisterComponent },

    // otherwise redirect to home
    { path: '**', redirectTo: '' }];
