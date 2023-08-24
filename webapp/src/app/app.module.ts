import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER } from '@angular/core';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MainNavComponent } from './main-nav/main-nav.component';
import { LayoutModule } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'
import { MatTableModule } from '@angular/material/table'
import { MatTooltipModule } from '@angular/material/tooltip';

import { ModularisationComponent } from './modularisation/modularisation.component';
import { AboutComponent } from './about/about.component';
import { CustomHttpInterceptor } from './shared/custom-http-interceptor';

import { AppConfig } from './app.config.service';

export function initialiseApp(appConfig: AppConfig) {
  return () => appConfig.loadSettings();
}

@NgModule({
  declarations: [
    AppComponent,
    MainNavComponent,
    ModularisationComponent,
    AboutComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatTableModule,
    MatTooltipModule,
    FormsModule,
    HttpClientModule,
    MatProgressSpinnerModule
  ],
  providers: [ 
    { provide: HTTP_INTERCEPTORS, useClass: CustomHttpInterceptor, multi: true},
    AppConfig,
    { provide: APP_INITIALIZER, useFactory: initialiseApp, deps: [AppConfig], multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
