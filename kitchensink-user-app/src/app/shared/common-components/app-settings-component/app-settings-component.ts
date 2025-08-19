import {Component, inject, OnInit} from '@angular/core';
import {AppSettingsService} from '../../../core/services/AppSettingsService';
import {MaterialModule} from '../../../material.module';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {FormsModule} from '@angular/forms';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';

@Component({
  selector: 'app-app-settings-component',
  imports: [MaterialModule, MatSlideToggle, FormsModule, MatSlider, MatSliderThumb],
  templateUrl: './app-settings-component.html',
  styleUrl: './app-settings-component.css'
})
export class AppSettingsComponent implements OnInit {
  private settingsService = inject(AppSettingsService);

  settings = {
    darkMode: false,
    primaryColor: 'indigo',
    fontSize: 14,
    language: 'en',
    dateFormat: 'MM/dd/yyyy',
    notifications: {
      push: true,
      email: true,
      sound: true
    },
    animations: true,
    autoSave: true,
    analytics: false,
    rememberLogin: false,
    colorClassList:[""]
  };

  ngOnInit() {
    this.loadSettings();
  }

  loadSettings() {
    this.settings = this.settingsService.getSettings();
  }

  onDarkModeChange(event: any) {
    this.settingsService.setDarkMode(event.checked);
  }

  onPrimaryColorChange(color: string) {
    this.settingsService.setPrimaryColor(color);
  }

  onFontSizeChange(event: any) {
    this.settingsService.applyFontSize(event);
  }

  onLanguageChange(language: string) {
    this.settingsService.setLanguage(language);
  }

  onDateFormatChange(format: string) {
    this.settingsService.setDateFormat(format);
  }

  onNotificationChange(type: string, event: any) {
    this.settingsService.setNotificationSetting(type, event.checked);
  }

  onAnimationsChange(event: any) {
    this.settingsService.setAnimations(event.checked);
  }

  onAutoSaveChange(event: any) {
    this.settingsService.setAutoSave(event.checked);
  }

  onAnalyticsChange(event: any) {
    this.settingsService.setAnalytics(event.checked);
  }

  onRememberLoginChange(event: any) {
    this.settingsService.setRememberLogin(event.checked);
  }

  saveSettings() {
    this.settingsService.saveSettings(this.settings);
    // Show success message (you can implement a snackbar here)
    console.log('Settings saved successfully!');
  }

  resetSettings() {
    this.settingsService.resetSettings();
    this.loadSettings();
    // Show reset message
    console.log('Settings reset to default!');
  }
}
