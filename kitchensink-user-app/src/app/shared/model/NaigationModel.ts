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
      route: 'admin-landing', // Change the route to a specific handler
      icon: 'dashboard',
      allowedRoles: ["ADMIN", "USER"] // All users can click on a dashboard link
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
