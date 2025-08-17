import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../../../../shared/model/UserModel';
import { UserService } from '../../../../core/services/UserService';
import { EditUserComponent } from '../edit-user/edit-user.component';
import { DeleteUserComponent } from '../delete-user/delete-user.component';
import { MaterialModule } from '../../../../material.module';
import { CommonModule, DatePipe } from '@angular/common';
import {Router} from '@angular/router';
import {DeleteResponse} from '../../../../core/services/AuthService';
import {
  ConfirmDialogComponent
} from '../../../../shared/common-components/confirm-dialog.component/confirm-dialog.component';
import {LoaderService} from '../../../../core/services/LoaderService';
import {AppSnackbarComponent} from '../../../../shared/common-components/app-snackbar/app-snackbar';

@Component({
  selector: 'users-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  imports: [MaterialModule, DatePipe, CommonModule, MatSort]
})
export class UserListComponent implements OnInit, AfterViewInit {
  searchBy: string = 'all';
  searchQuery: string = '';
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
  sortField: string = 'username'; // default sort column
  sortDirection: 'asc' | 'desc' = 'asc';
  dataSource: MatTableDataSource<User> = new MatTableDataSource<User>();
  totalRecords: number = 0;
  pageSize: number = 5;
  pageIndex: number = 0;
  sortBy: string = 'username';
  filterValue: string = '';

  @ViewChild(MatSort, {static: false}) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatTable) table!: MatTable<any>;
  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router,
    private loaderService: LoaderService
  ) {
  }

  ngOnInit(): void {
    this.loadUsers();
    this.dataSource.filterPredicate = (data: User, filter: string): boolean => {
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
    this.dataSource.sort = this.sort;
    this.paginator.page.subscribe(() => {
      this.pageIndex = this.paginator.pageIndex;
      this.pageSize = this.paginator.pageSize;
    });
    this.sort.sortChange.subscribe((sortState: Sort) => {
      this.pageIndex = 0;
      this.sortBy = sortState.active;
      this.sortDirection = sortState.direction || 'asc';
    });

    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers(this.pageIndex, this.pageSize, this.sortBy, this.sortDirection)
      .subscribe({
        next: (data) => {
          if(data?.content?.length>0) {
            this.dataSource.data = data.content;
            this.totalRecords = data.totalElements as number;
          } else {
            this.showMessage("No results found");
          }

        },
        error: (err) => {
          this.loaderService.hide();
          let errorMessage = err.error.message ;
          this.snackBar.openFromComponent(AppSnackbarComponent, {
            data: { errorMessage },
            duration: 3000,
            verticalPosition: 'top', // position at the top
            panelClass: ['error-snackbar'] // custom CSS
          });
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
  }
  resetTable(): void {
    this.dataSource.data = [];
    this.totalRecords = 0;
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
  }

  viewUserDetails(user: User): void {
    this.router.navigate(['/dashboard/user-details'], { state: { email: user.email} });
  }

  editUser(user: User) {
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate(['dashboard/edit-user'], { state: { email:  user.email} });
    });
  }

  deleteUser(user: any): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {title: 'Confirm Delete', message: `Are you sure you want to delete ${user.email}?`}
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.userService.deleteUser(user.email).subscribe({
          next: (res) => {
            this.snackBar.open(res.message || 'User deleted successfully', 'Close', {duration: 3000});
            this.loadUsers();
          },
          error: (err) => {
            let errorMessage = err.error.message ;
            this.snackBar.openFromComponent(AppSnackbarComponent, {
              data: { errorMessage },
              duration: 3000,
              verticalPosition: 'top', // position at the top
              panelClass: ['error-snackbar'] // custom CSS
            });
          }
        });
      }
    });
  }
  onSearch(): void {
    switch (this.searchBy) {
      case 'name':
        this.searchByName();
        break;
      case 'email':
        this.searchByEmail();
        break;
      case 'city':
        this.searchByCity();
        break;
      case 'country':
        this.searchByCountry();
        break;
      case 'all':
      default:
        this.loadUsers();
    }
  }
  // Example methods
  searchByName() {
    this.loaderService.show();
    this.resetTable();
    this.userService.getUserByName(this.searchQuery, 0, 100, this.sortBy, this.sortDirection).subscribe({
      next: (res) => {
        if(res?.content?.length>0) {
          this.dataSource.data = res.content;
          this.totalRecords = res.totalElements as number;
          this.table.renderRows();
        } else {
          this.dataSource.data = [];
          this.showMessage("No results found");
        }
        this.loaderService.hide();
      },
      error: (err) => {
        this.loaderService.hide();
        this.dataSource.data = [];
        let errorMessage = err.error.message ;
        this.showMessage(errorMessage + ":"+this.searchQuery);
      }
    });
  }

  searchByCity() {
    this.loaderService.show();
    this.dataSource = new MatTableDataSource<User>();
    this.userService.getUserByCity(this.searchQuery, 0, 100, this.sortBy, this.sortDirection).subscribe({
      next: (res) => {
        if(res?.content?.length>0) {
          this.dataSource.data = res.content;
          this.totalRecords = res.totalElements as number;
          this.table.renderRows();
        } else {
          this.showMessage("No results found");
        }
        this.loaderService.hide();
      },
      error: (err) => {
        this.loaderService.hide();
        this.dataSource.data = [];
        let errorMessage = err.error.message ;
        this.showMessage(errorMessage + ":"+this.searchQuery);
      }
    });
  }
  searchByEmail() {
    this.loaderService.show();
    this.resetTable();
    this.userService.searchByEmail(this.searchQuery,0, 100, this.sortBy, this.sortDirection).subscribe({
      next: (res) => {
        if(res?.content?.length>0) {
          this.dataSource.data = res.content;
          this.totalRecords = res.totalElements as number;
          this.table.renderRows();
        } else {
          this.showMessage("No results found with this email: "+ this.searchQuery);
        }
        this.loaderService.hide();
      },
      error: (err) => {
        this.loaderService.hide();
        this.dataSource.data = [];
        let errorMessage = err.error.message ;
      }
    });
  }
  searchByCountry() {
    this.loaderService.show();
    this.resetTable();
    this.userService.searchByCountry(this.searchQuery, 0, 100, this.sortBy, this.sortDirection).subscribe({
      next: (res) => {
        if(res?.content?.length>0) {
          this.dataSource.data = res.content;
          this.totalRecords = res.totalElements as number;
          this.table.renderRows();
        } else {
          this.showMessage("No results found");
        }
        this.loaderService.hide();
      },
      error: (err) => {
        this.dataSource.data = [];
        this.loaderService.hide();
        let errorMessage = err.error.message ;
        this.showMessage(errorMessage + ":"+this.searchQuery);
      }
    });
  }
  showMessage(message: string) {
    this.snackBar.openFromComponent(AppSnackbarComponent, {
      data: { message },
      duration: 3000,
      verticalPosition: 'top', // position at the top
      panelClass: ['error-snackbar'] // custom CSS
    });
  }

  applySort() {
    if (!this.sortField) return;

    const field = this.sortField;
    const direction = this.sortDirection === 'asc' ? 1 : -1;

    this.dataSource.data = this.dataSource.data.slice().sort((a: any, b: any) => {
      let valueA: any;
      let valueB: any;

      switch (field) {
        case 'name':
          valueA = `${a.profile?.firstName || ''} ${a.profile?.lastName || ''}`.toLowerCase();
          valueB = `${b.profile?.firstName || ''} ${b.profile?.lastName || ''}`.toLowerCase();
          break;
        case 'city':
          valueA = (a.profile?.city || '').toLowerCase();
          valueB = (b.profile?.city || '').toLowerCase();
          break;
        case 'country':
          valueA = (a.profile?.country || '').toLowerCase();
          valueB = (b.profile?.country || '').toLowerCase();
          break;
        case 'createdAt':
          valueA = new Date(a.createdAt).getTime();
          valueB = new Date(b.createdAt).getTime();
          break;
        default:
          valueA = (a[field] || '').toString().toLowerCase();
          valueB = (b[field] || '').toString().toLowerCase();
      }

      if (valueA < valueB) return -1 * direction;
      if (valueA > valueB) return 1 * direction;
      return 0;
    });
  }
  toggleSortDirection() {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    this.pageIndex = 0;
    this.applySort();
  }
}
