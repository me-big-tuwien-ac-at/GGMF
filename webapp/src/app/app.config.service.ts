import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppConfigSetting } from './interface/app-config.interface';

const APP_SETTINGS_FILE_PATH = 'assets/config/config.json'

@Injectable({
    providedIn: 'root'
  })
export class AppConfig {
  static settings: AppConfigSetting;

  constructor(private httpClient: HttpClient) { }

  loadSettings() {
    return new Promise<void>((resolve, reject) => {
      this.httpClient.get(APP_SETTINGS_FILE_PATH).toPromise().then((response : AppConfigSetting) => {
         AppConfig.settings = <AppConfigSetting> response;
         resolve();
      }).catch((response: any) => {
         reject(`Could not load file from '${APP_SETTINGS_FILE_PATH}': ${JSON.stringify(response)}`);
      });
    });
  }
}