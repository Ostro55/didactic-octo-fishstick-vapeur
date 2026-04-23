import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse, PhotoInfo} from "./GameURL";

export interface Game {
    description: string | null;
    editor: string | null;
    genre: string[];
    id: number;
    img_url: string | null;
    name: string;
    price: number;
    release_date: string | null;
    status: string | null;
}

export class LoginUser {
    public username:         string;
    public email:            string;
    public password:         string;

    constructor(
        username: string = "",
        email: string = "",
        password: string = ""
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}



export class GameRequest {
    public name: string;
    public price: number;
    public genre: string[];
    public makingTime: Date;
    public userId: string | undefined;
    public description: string | null;
    public img_url: string | null;
    public editor: string | null;

    constructor(
        name: string = "",
        price: number = 1,
        genre: string[] = [],
        makingTime: Date = new Date(Date.now()),
        id:string | undefined,
        description: string | null = null,
        img_url: string | null = null,
        editor: string | null = null
    ) {
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.makingTime = makingTime;
        this.userId = id;
        this.description = description;
        this.img_url = img_url;
        this.editor = editor;
    }

}


export interface GamePage {
    page: number;
    pages: number;
    perpage: number;
    total: number;
    photo: Game[];
}

export interface ApiResponse {
    photos: GamePage;
    stat: string;
}

export class GameSmall {
    description: string = "" ;
    editor: string = "";
    genre: string[] = [];
    id: number = -1;
    img_url: string  = "";
    name: string = "";
    price: number = 0;
    release_date: string = "";
    status: string = "";
    makingTime: string = "2026-03-26T14:06:44.063Z";
    public image: BehaviorSubject<FlickrPhotoResponse  | undefined> = new BehaviorSubject<FlickrPhotoResponse | undefined>( undefined);


    public static GameSmall2()
    {
        let p = new GameSmall();
        p.id =- -1;
        p.name = "";
        p.price = 0;
        p.genre = [];
        p.makingTime = "2026-03-26T14:06:44.063Z";

        return p;
    }

}


