import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ModularisationComponent } from './modularisation/modularisation.component';
import { AboutComponent } from './about/about.component'

const routes: Routes = [
  { path: 'modularisation', component: ModularisationComponent },
  { path: 'about', component: AboutComponent },
  { path: '', redirectTo: '/modularisation', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
