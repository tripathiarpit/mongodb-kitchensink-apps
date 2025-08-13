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
      allowedRoles: ['USER', 'ADMIN']
    },
    {
      label: 'Admin Panel',
      route: '/admin',
      icon: 'admin_panel_settings',
      allowedRoles: ['ADMIN']
    },
    {
      label: 'User Access Control',
      route: '/admin',
      icon: 'admin_panel_settings',
      allowedRoles: ['ADMIN']
    },
    {
      label: 'Settings',
      route: '/settings',
      icon: 'settings',
      allowedRoles: ['USER', 'ADMIN']
    },
    {
      label: 'Reports',
      route: '/reports',
      icon: 'assessment',
      allowedRoles: ['ADMIN']
    },
  ];

  static getMenuItemsForRole(userRole: string): NavItem[] {
    return this.menuItems.filter(item => item.allowedRoles.includes(userRole));
  }
}
