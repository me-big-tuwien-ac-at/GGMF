import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SpinnerService {
  readonly visibility: BehaviorSubject<boolean>;

  constructor() {
    this.visibility = new BehaviorSubject(false); 
  }

  public displayProgressSpinner(): void {
    this.visibility.next(true)
  }

  public hideProgressSpinner(): void {
    this.visibility.next(false)
  } 
}
