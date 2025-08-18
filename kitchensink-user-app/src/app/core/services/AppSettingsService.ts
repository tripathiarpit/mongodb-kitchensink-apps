import { Injectable, inject, Renderer2, RendererFactory2, DOCUMENT } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface AppSettings {
  darkMode: boolean;
  primaryColor: string;
  fontSize: number;
  language: string;
  dateFormat: string;
  notifications: {
    push: boolean;
    email: boolean;
    sound: boolean;
  };
  animations: boolean;
  autoSave: boolean;
  analytics: boolean;
  rememberLogin: boolean;
  colorClassList: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AppSettingsService {
  private document = inject(DOCUMENT);
  private rendererFactory = inject(RendererFactory2);
  private renderer: Renderer2;

  private readonly STORAGE_KEY = 'app-settings';
  private colorClasses = [
    'indigo',
    'pink',
    'purple',
    'deep-purple',
    'blue',
    'teal',
    'green',
    'orange',
    'slate',
    'stone',
    'cyan',
    'emerald',
    'rose',
    'amber',
    'lime',
    'sky',
    'violet',
    'fuchsia',
    'neutral',
    'zinc'
  ];
  private defaultSettings: AppSettings = {
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
    colorClassList: this.colorClasses
  };

  private settingsSubject = new BehaviorSubject<AppSettings>(this.defaultSettings);
  public settings$ = this.settingsSubject.asObservable();

  private currentSettings: AppSettings;

  constructor() {
    this.renderer = this.rendererFactory.createRenderer(null, null);
    this.currentSettings = this.loadSettingsFromStorage();
    this.settingsSubject.next(this.currentSettings);
    this.applySettings(this.currentSettings);
  }

  private loadSettingsFromStorage(): AppSettings {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const parsedSettings = JSON.parse(stored);
        return { ...this.defaultSettings, ...parsedSettings };
      }
    } catch (error) {
      console.warn('Failed to load settings from localStorage:', error);
    }
    return { ...this.defaultSettings };
  }

  private saveSettingsToStorage(settings: AppSettings): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(settings));
    } catch (error) {
      console.warn('Failed to save settings to localStorage:', error);
    }
  }

  private applySettings(settings: AppSettings): void {
    // Apply dark mode
    this.applyDarkMode(settings.darkMode);

    // Apply primary color theme
    this.applyPrimaryColor(settings.primaryColor);

    // Apply font size
    this.applyFontSize(settings.fontSize);

    // Apply animations
    this.applyAnimations(settings.animations);

    // Apply language (this would typically integrate with i18n)
    this.applyLanguage(settings.language);
  }

  private applyDarkMode(isDark: boolean): void {
    const body = this.document.body;
    if (isDark) {
      this.renderer.addClass(body, 'virbrant-theme');
      this.renderer.removeClass(body, 'light-theme');
    } else {
      this.renderer.addClass(body, 'light-theme');
      this.renderer.removeClass(body, 'virbrant-theme');
    }
  }

  private applyPrimaryColor(color: string): void {
    const body = this.document.body;


    this.colorClasses.forEach(cls => {
      this.renderer.removeClass(body, `theme-${cls}`);
    });

    // Add new color class
    this.renderer.addClass(body, `theme-${color}`);

    const colorMap: { [key: string]: string } = {
      'indigo': '#3f51b5',
      'pink': '#e91e63',
      'purple': '#9c27b0',
      'deep-purple': '#673ab7',
      'blue': '#2196f3',
      'teal': '#009688',
      'green': '#4caf50',
      'orange': '#ff9800',
      'slate': '#64748b',       // muted bluish gray
      'stone': '#78716c',       // soft earthy gray
      'cyan': '#06b6d4',        // modern cyan
      'emerald': '#10b981',     // fresh muted green
      'rose': '#f43f5e',        // soft rose red
      'amber': '#f59e0b',       // warm amber
      'lime': '#84cc16',        // natural lime
      'sky': '#0ea5e9',         // light blue
      'violet': '#8b5cf6',      // elegant violet
      'fuchsia': '#d946ef',     // modern magenta
      'neutral': '#9ca3af',     // balanced gray
      'zinc': '#71717a'         // cool muted gray
    };

    this.document.documentElement.style.setProperty('--primary-color', colorMap[color] || colorMap['indigo']);
  }

  private applyFontSize(fontSize: number): void {
    this.document.documentElement.style.setProperty('--base-font-size', `${fontSize}px`);
  }

  private applyAnimations(enabled: boolean): void {
    const body = this.document.body;
    if (enabled) {
      this.renderer.removeClass(body, 'no-animations');
    } else {
      this.renderer.addClass(body, 'no-animations');
    }
  }

  private applyLanguage(language: string): void {
    // This is where you would integrate with Angular i18n
    // For now, we'll just set the lang attribute
    this.renderer.setAttribute(this.document.documentElement, 'lang', language);
  }

  private updateSettings(updates: Partial<AppSettings>): void {
    this.currentSettings = { ...this.currentSettings, ...updates };
    this.settingsSubject.next(this.currentSettings);
    this.saveSettingsToStorage(this.currentSettings);
  }

  // Public methods
  getSettings(): AppSettings {
    return { ...this.currentSettings };
  }

  setDarkMode(isDark: boolean): void {
    this.updateSettings({ darkMode: isDark });
    this.applyDarkMode(isDark);
  }

  setPrimaryColor(color: string): void {
    this.updateSettings({ primaryColor: color });
    this.applyPrimaryColor(color);
  }

  setFontSize(fontSize: number): void {
    this.updateSettings({ fontSize });
    this.applyFontSize(fontSize);
  }

  setLanguage(language: string): void {
    this.updateSettings({ language });
    this.applyLanguage(language);
  }

  setDateFormat(format: string): void {
    this.updateSettings({ dateFormat: format });
  }

  setNotificationSetting(type: string, value: boolean): void {
    const notifications = { ...this.currentSettings.notifications };
    (notifications as any)[type] = value;
    this.updateSettings({ notifications });
  }

  setAnimations(enabled: boolean): void {
    this.updateSettings({ animations: enabled });
    this.applyAnimations(enabled);
  }

  setAutoSave(enabled: boolean): void {
    this.updateSettings({ autoSave: enabled });
  }

  setAnalytics(enabled: boolean): void {
    this.updateSettings({ analytics: enabled });
  }

  setRememberLogin(enabled: boolean): void {
    this.updateSettings({ rememberLogin: enabled });
  }

  saveSettings(settings: AppSettings): void {
    this.currentSettings = { ...settings };
    this.settingsSubject.next(this.currentSettings);
    this.saveSettingsToStorage(this.currentSettings);
    this.applySettings(this.currentSettings);
  }

  resetSettings(): void {
    this.currentSettings = { ...this.defaultSettings };
    this.settingsSubject.next(this.currentSettings);
    this.saveSettingsToStorage(this.currentSettings);
    this.applySettings(this.currentSettings);
  }

  // Observable for components to subscribe to settings changes
  getSettingsObservable(): Observable<AppSettings> {
    return this.settings$;
  }
}
