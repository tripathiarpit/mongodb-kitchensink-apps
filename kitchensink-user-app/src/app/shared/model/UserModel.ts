export interface Profile {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  street: string;
  city: string;
  state: string;
  country: string;
  pincode: string;
}

export interface User {
  id: string | undefined;
  email: string | undefined;
  username: string;
  isAccountVerificationPending?: boolean;
  roles: string[];
  active: boolean | undefined;
  twoFAEnabled: boolean | undefined;
  createdAt: string; // ISO string from backend
  profile: Profile;
}

export interface UserPage {
  content: User[];
  totalElements: number | undefined;
  totalPages: number | undefined;
  number: number | undefined;
}

