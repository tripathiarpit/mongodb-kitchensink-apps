import { Component, Inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MaterialModule} from '../../../../material.module';
import {ReactiveFormsModule} from '@angular/forms';
import {UserService} from '../../../../core/services/UserService';

@Component({
  selector: 'app-delete-user',
  imports:[MaterialModule, ReactiveFormsModule],
  template: `
    <h2 mat-dialog-title>Confirm Delete</h2>
    <mat-dialog-content>
      Are you sure you want to delete user <strong>{{data.username}}</strong>?
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Cancel</button>
      <button mat-raised-button color="warn" (click)="confirm()">Delete</button>
    </mat-dialog-actions>
  `
})
export class DeleteUserComponent {

  constructor(
    public dialogRef: MatDialogRef<DeleteUserComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private userService: UserService,
  ) {}

  cancel() {
    this.dialogRef.close(false);
  }

  confirm() {
    this.userService.deleteUser(this.data.email).subscribe((data) => {

    });
    this.dialogRef.close(true);
  }
}
