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
      route: '',
      icon: 'dashboard',
      allowedRoles: ["ADMIN"]
    },
    {
      label: 'Admin Panel',
      route: '/admin',
      icon: 'admin_panel_settings',
      allowedRoles: ["ADMIN"]
    },
    {
      label: 'User Management',
      route: '/user-management',
      icon: 'admin_panel_settings',
      allowedRoles: ["ADMIN","USER"]
    },
    {
      label: 'Settings',
      route: '/settings',
      icon: 'settings',
      allowedRoles: ["ADMIN","USER"]
    },
    {
      label: 'Reports',
      route: '/reports',
      icon: 'assessment',
      allowedRoles: ["USER"]
    },
  ];

  static getMenuItemsForRole(userRole: string): NavItem[] {
    return this.menuItems.filter(item => item.allowedRoles.includes(userRole));
  }
}
