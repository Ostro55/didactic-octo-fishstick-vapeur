import {BehaviorSubject} from "rxjs";
import {FlickrPhotoResponse, PhotoInfo} from "./PhotoURL";

export interface Photo {
    id: string;
    owner: string;
    secret: string;
    server: string;
    farm: number;
    title: string;
    ispublic: number;
    isfriend: number;
    isfamily: number;

    url_sq: string;
    height_sq: number;
    width_sq: number;

    url_t: string;
    height_t: number;
    width_t: number;

    url_s: string;
    height_s: number;
    width_s: number;

    url_q: string;
    height_q: number;
    width_q: number;

    url_m: string;
    height_m: number;
    width_m: number;

    url_n: string;
    height_n: number;
    width_n: number;

    url_z: string;
    height_z: number;
    width_z: number;

    url_c: string;
    height_c: number;
    width_c: number;

    url_l: string;
    height_l: number;
    width_l: number;

    url_o: string;
    height_o: number;
    width_o: number;
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
    public id: string ="";
    public title: string = "";
    public url: string = ""
    public image: BehaviorSubject<FlickrPhotoResponse  | undefined> | undefined = new BehaviorSubject<FlickrPhotoResponse | undefined>( undefined);
    public height: number =0;
    public width: number =0;

    public urlthumb: string = ""
    public heightthumb: number =0;
    public widththumb: number =0;

    public static PhotoSmall2(urlthumb: string)
    {
        let p = new PhotoSmall();
        p.urlthumb = urlthumb;
        p.heightthumb = 10;
        p.widththumb = 10;
        p.image = undefined
        return p;
    }
    public PhotoSmall(id:string,title:string,url :string,height:number,width:number,urlthumb :string,heightthumb:number,widththumb:number)
    {
        this.id = id;
        this.title = title;
        this.url = url;
        this.width = width;
        this.height = height;
        this.urlthumb = urlthumb;
        this.widththumb=widththumb;
        this.heightthumb = heightthumb;
    }
}


