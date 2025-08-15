import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';
import {BaseUrlInterceptor} from './core/interceptors/BaseUrlInterceptor';
import {JwtInterceptor} from './core/interceptors/JwtInterceptor';
import {AuthInterceptor} from './core/interceptors/AuthInterceptor';


export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(BrowserAnimationsModule),
    provideRouter(routes),


    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: BaseUrlInterceptor, multi: true },
    {provide: HTTP_INTERCEPTORS,useClass: AuthInterceptor,multi: true}
  ],
};
