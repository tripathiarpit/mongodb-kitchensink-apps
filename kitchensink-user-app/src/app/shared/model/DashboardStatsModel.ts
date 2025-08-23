export interface DashboardStats {
  bothAdminAndUser: number
  totalUsers: number;
  activeUsers: number;
  pendingVerifications: number;
  firstTimeLogins: number;
  newUsersThisMonth: number;
  serverTime: string;
  adminUsers:number;
  regularUsers:number;
  avgResponseTimeMs?:number;
  uptimeSeconds?: number
}
