import { Component, OnInit } from '@angular/core';
import {User, UserService} from '../../../../core/services/UserService';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  standalone: false
})
export class UserListComponent implements OnInit {
  users: User[] = [];

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.getUsers().subscribe(users => this.users = users);
  }
}
