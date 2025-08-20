export interface NavItem {
  label: string;
  route?: string;
  icon: string;
  allowedRoles: string[];
  children?: NavItem[];
}

export class NavigationMenu {
  static allMenuItems: NavItem[] = [
    {
      label: 'Dashboard',
      route: 'admin-landing',
      icon: 'mdi mdi-home',
      allowedRoles: ["ADMIN", "USER"]
    },
    {
      label: 'User Management',
      route: 'user-management',
      icon: 'mdi mdi-account-box',
      allowedRoles: ["ADMIN"]
    }

  ];

  static getMenuItemsForRole(userRole: string): NavItem[] {
    const filteredItems: NavItem[] = [];

    // Iterate through each top-level menu item
    for (const item of this.allMenuItems) {
      // Check if the current user role is allowed to see this item
      if (item.allowedRoles.includes(userRole)) {
        // Create a copy of the item to avoid modifying the original data
        const newItem: NavItem = { ...item };

        // If the item has children, recursively filter them
        if (newItem.children) {
          newItem.children = newItem.children.filter(child =>
            child.allowedRoles.includes(userRole)
          );

          // Only add the parent if it has at least one child after filtering
          // OR if it's a top-level item with no children
          if (newItem.children.length > 0 || !newItem.route) {
            filteredItems.push(newItem);
          }
        } else {
          // If the item has no children, just add it
          filteredItems.push(newItem);
        }
      }
    }
    return filteredItems;
  }
}
