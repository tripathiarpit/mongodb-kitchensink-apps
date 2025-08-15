import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../../../../shared/model/UserModel';
import { UserService } from '../../../../core/services/UserService';
import { EditUserComponent } from '../edit-user/edit-user.component';
import { DeleteUserComponent } from '../delete-user/delete-user.component';
import { MaterialModule } from '../../../../material.module';
import { CommonModule, DatePipe } from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'users-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  imports: [MaterialModule, DatePipe, CommonModule, MatSort]
})
export class UserListComponent implements OnInit, AfterViewInit {

  displayedColumns: string[] = [
    'count',      // ✅ Added numbering column
    'name',
    'email',
    'phone',
    'username',
    'city',
    'country',
    'roles',
    'active',
    'createdAt',
    'actions'
  ];

  dataSource: MatTableDataSource<User> = new MatTableDataSource<User>();
  totalRecords: number = 0;
  pageSize: number = 10;
  pageIndex: number = 0;
  sortBy: string = 'username';
  sortDirection: string = 'asc';
  filterValue: string = '';

  @ViewChild(MatSort, { static: false }) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.dataSource.filterPredicate = (data: User, filter: string):boolean => {
      const searchTerm = filter.trim().toLowerCase();
      return <boolean>(
        (data.profile.firstName && data.profile.firstName.toLowerCase().includes(searchTerm)) ||
        (data.email && data.email.toLowerCase().includes(searchTerm)) ||
        (data.profile.city && data.profile.city.toLowerCase().includes(searchTerm)) ||
        (data.profile.country && data.profile.country.toLowerCase().includes(searchTerm))
      );
    };
  }

  ngAfterViewInit() {
    // Paginator change
    this.paginator.page.subscribe(() => {
      this.pageIndex = this.paginator.pageIndex;
      this.pageSize = this.paginator.pageSize;
      this.loadUsers();
    });
    this.sort.sortChange.subscribe((sortState: Sort) => {
      this.pageIndex = 0;
      this.sortBy = sortState.active;
      this.sortDirection = sortState.direction || 'asc';
      this.loadUsers();
    });

    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers(this.pageIndex, this.pageSize, this.sortBy, this.sortDirection)
      .subscribe({
        next: (data) => {
          this.dataSource.data = data.content;
          this.totalRecords = data.totalElements as number;
        },
        error: (err) => {
          console.log(err);
          this.snackBar.open('Error fetching users', 'Close', { duration: 3000 });
        }
      });
  }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.filterValue = filterValue.trim().toLowerCase();
    this.dataSource.filter = this.filterValue;
  }

  sortChange(event: any) {
    this.sortBy = event.active;
    this.sortDirection = event.direction || 'asc';
    this.loadUsers();
  }

  pageChange(event: any) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  viewUserDetails(user: User): void {

  }

  editUser(user: User) {
    this.router.navigate(['dashboard/edit-user', user.email]);
  }

  deleteUser(user: User) {
    const dialogRef = this.dialog.open(DeleteUserComponent, {
      width: '400px',
      data: user
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.userService.deleteUser(user.id as string).subscribe({
          next: () => this.loadUsers(),
          error: () => this.snackBar.open('Error deleting user', 'Close', { duration: 3000 })
        });
      }
    });
  }
}
