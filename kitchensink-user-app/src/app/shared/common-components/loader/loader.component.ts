import {Component, OnInit} from '@angular/core';
import {LoaderService} from '../../../core/services/LoaderService';
import {MaterialModule} from '../../../material.module';
import {Observable} from 'rxjs';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {AsyncPipe} from '@angular/common';


@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss'],
  imports: [MaterialModule, MatProgressSpinner, AsyncPipe]
})
export class LoaderComponent implements OnInit {
  loading$!: Observable<boolean>;

  constructor(private loaderService: LoaderService) {}

  ngOnInit(): void {
    this.loading$ = this.loaderService.isLoading();
  }
}
