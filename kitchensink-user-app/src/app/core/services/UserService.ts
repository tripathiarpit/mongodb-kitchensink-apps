import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';
import {RegistrationRequest, RegistrationResponse} from '../../shared/model/UserRegistrationModel';
import {User, UserPage} from '../../shared/model/UserModel';


@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  registerUser(request: RegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.apiUrl}/register`, request);
  }

  getUsers(page: number, size: number, sortBy: string, direction: string): Observable<UserPage> {
    return this.http.get<UserPage>('/api/users', {
      params: new HttpParams()
        .set('page', page)
        .set('size', size)
        .set('sortBy', sortBy)
        .set('direction', direction)
    });
  }
  deleteUser(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/delete`, { email });
  }
  updateUser(emailId: string, user: any): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${emailId}`, user);
  }
  gerUserByEmailId(email: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/email/${email}`);
  }

  getUserByName(searchQuery: string, page: number, size: number, sortBy: string, direction: string) : Observable<UserPage> {
    return this.http.get<UserPage>('/api/users/getUserByName', {
      params: new HttpParams()
        .set('page', page)
        .set('size', size)
        .set('sortBy', sortBy)
        .set('direction', direction)
        .set('name', searchQuery)

    });
  }

  getUserByCity(searchQuery: string, page: number, size: number, sortBy: string, direction: string) : Observable<UserPage> {
    return this.http.get<UserPage>('/api/users/getUserByCity', {
      params: new HttpParams()
        .set('page', page)
        .set('size', size)
        .set('sortBy', sortBy)
        .set('direction', direction)
        .set('city', searchQuery)
    });
  }

  searchByEmail(searchQuery: string,page: number, size: number, sortBy: string, direction: string) : Observable<UserPage> {
    return this.http.get<UserPage>('/api/users/getUserByEmail', {
      params: new HttpParams()
        .set('page', page)
        .set('size', size)
        .set('sortBy', sortBy)
        .set('direction', direction)
        .set('email', searchQuery)
    });
  }

  searchByCountry(searchQuery: string,page: number, size: number, sortBy: string, direction: string) : Observable<UserPage> {
    return this.http.get<UserPage>('/api/users/getUserByCountry', {
      params: new HttpParams()
        .set('page', page)
        .set('size', size)
        .set('sortBy', sortBy)
        .set('direction', direction)
        .set('country', searchQuery)
    });
  }
}
