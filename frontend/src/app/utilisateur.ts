export interface Utilisateur {
    email: string;
    password: string;
}

export class UserConnectd {

    public email: string = "";
    public  id : string ="";
    public  isAdmin: boolean =false;
    constructor(email: string, id: string, isAdmin: boolean) {
        this.email = email
        this.id = id
        this.isAdmin = isAdmin
    }

}