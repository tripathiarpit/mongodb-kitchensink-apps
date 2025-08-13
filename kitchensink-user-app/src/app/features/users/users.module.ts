import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {UserListComponent} from './components/user-list/UserListComponent';
import {MaterialModule} from '../../material.module';

@NgModule({
  declarations: [
    UserListComponent
  ],
  imports: [
    CommonModule,
    MaterialModule
  ],

})
export class UserModule { }
