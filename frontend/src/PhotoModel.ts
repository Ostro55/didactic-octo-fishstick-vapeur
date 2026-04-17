import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse, PhotoInfo} from "./PhotoURL";

export interface Photo {
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

export interface PhotosPage {
    page: number;
    pages: number;
    perpage: number;
    total: number;
    photo: Photo[];
}

export interface ApiResponse {
    photos: PhotosPage;
    stat: string;
}

export class PhotoSmall {
    description: string = "" ;
    editor: string = "";
    genre: string[] = [];
    id: number = -1;
    img_url: string  = "";
    name: string = "";
    price: number = 0;
    release_date: string = "";
    status: string = " ";
    makingTime: string = "2026-03-26T14:06:44.063Z";
    public image: BehaviorSubject<FlickrPhotoResponse  | undefined> = new BehaviorSubject<FlickrPhotoResponse | undefined>( undefined);


    public static PhotoSmall2()
    {
        let p = new PhotoSmall();
        p.id = -1;
        p.name = "";
        p.price = 0;
        p.genre = [];
        p.makingTime = "2026-03-26T14:06:44.063Z";

        return p;
    }

    public PhotoSmall(
        id: number,
        name: string,
        price: number,
        genre: string[],
        makingTime: string
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.makingTime = makingTime;
        return this;
    }
}


