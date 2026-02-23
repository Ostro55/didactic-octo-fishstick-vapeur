import { Routes } from '@angular/router';
import {UserPage} from "./user-page/user-page";
import {HomePage} from "./home-page/home-page";
import {SearchBar} from "./search-bar/search-bar";
import {SearchPage} from "./search-page/search-page";
import {AdminPage} from "./admin-page/admin-page";
import {LoginPage} from "./login-page/login-page";
import { AuthGuard } from './auth-guard';

export const routes: Routes = [

    { path: '', component: HomePage },
    { path: 'admin', component: AdminPage , canActivate: [AuthGuard] },
    { path: 'user', component: UserPage , canActivate: [AuthGuard]},
    { path: 'login', component: LoginPage},
    { path: 'search', component: SearchPage},

    //{ path: 'login', component: LoginComponent },
    //{ path: 'register', component: RegisterComponent },

    // otherwise redirect to home
    { path: '**', redirectTo: '' }];
