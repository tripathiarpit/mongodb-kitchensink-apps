// models/navigation.model.ts
export interface NavItem {
  label: string;
  route: string;
  icon?: string;
  allowedRoles: string[];
}

export class NavigationMenu {
  static menuItems: NavItem[] = [
    {
      label: 'Dashboard',
      route: 'admin-landing',
      icon: 'dashboard',
      allowedRoles: ["ADMIN", "USER"]
    },
    {
      label: 'App Resource Settings',
      route: 'resource-settings',
      icon: 'admin_panel_settings',
      allowedRoles: ["ADMIN"]
    },
    {
      label: 'User Management',
      route: 'user-management',
      icon: 'admin_panel_settings',
      allowedRoles: ["ADMIN"]
    }
  ];

  static getMenuItemsForRole(userRole: string): NavItem[] {
    return this.menuItems.filter(item => item.allowedRoles.includes(userRole));
  }
}
