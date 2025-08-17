import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {map} from 'rxjs/operators';

export interface Country {
  name: string;
  code: string;
}

@Injectable({
  providedIn: 'root'
})
export class CountryService {
  private apiUrl = "https://restcountries.com/v3.1/all?fields=name,cca2,cca3,flags"

  constructor(private http: HttpClient) {}

  getCountries(): Observable<Country[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(countries =>
        countries.map(c => ({
          name: c.name.common,
          code: c.cca2
        }))
      )
    );
  }
}
