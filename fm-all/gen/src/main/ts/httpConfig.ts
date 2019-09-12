import { Injectable } from '@angular/core';

@Injectable({providedIn:"root"})
export class HttpConfig{
    url = 'http://localhost:8080/a';
    auth = '123456789';
}