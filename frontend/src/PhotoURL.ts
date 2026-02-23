export interface FlickrPhotoResponse {
    photo: PhotoInfo;
    stat: string;
}

export interface PhotoInfo {
    id: string;
    secret: string;
    server: string;
    farm: number;
    dateuploaded: string;
    isfavorite: number;
    license: string;
    safety_level: string;
    rotation: number;
    originalsecret: string;
    originalformat: string;
    owner: Owner;
    title: ContentWrapper;
    description: ContentWrapper;
    visibility: Visibility;
    dates: Dates;
    views: string;
    editability: Editability;
    publiceditability: Editability;
    usage: Usage;
    comments: ContentWrapper;
    notes: Notes;
    people: People;
    tags: Tags;
    urls: Urls;
    media: string;
    location : Location
}
export interface Location{
        latitude: string,
        longitude: string,
        accuracy: string,
        context: string,
        locality: {
            _content: string,
        },
        county: {
            _content: string,
        },
        region: {
            "_content": string,
        },
        country: {
            "_content":string,
        },
        neighbourhood: {
            "_content": string,
        },
}
export interface Owner {
    nsid: string;
    username: string;
    realname: string;
    location: string;
    iconserver: string;
    iconfarm: number;
    path_alias: string;
    gift: Gift;
}

export interface Gift {
    gift_eligible: boolean;
    eligible_durations: string[];
    new_flow: boolean;
}

export interface ContentWrapper {
    _content: string;
}

export interface Visibility {
    ispublic: number;
    isfriend: number;
    isfamily: number;
}

export interface Dates {
    posted: string;
    taken: string;
    takengranularity: number;
    takenunknown: string;
    lastupdate: string;
}

export interface Editability {
    cancomment: number;
    canaddmeta: number;
}

export interface Usage {
    candownload: number;
    canblog: number;
    canprint: number;
    canshare: number;
}

export interface Notes {
    note: any[];
}

export interface People {
    haspeople: number;
}

export interface Tags {
    tag: Tag[];
}

export interface Tag {
    id: string;
    author: string;
    authorname: string;
    raw: string;
    _content: string;
    machine_tag: number;
}

export interface Urls {
    url: Url[];
}

export interface Url {
    type: string;
    _content: string;
}
