
export interface AddressRequest {
  street: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
}


export interface RegistrationRequest {
  firstName: string;
  lastName:string;
  email: string;
  password: string;
  phoneNumber: string;
  address: AddressRequest; // nested object
  city: string;            // root-level city
  pincode: string;         // root-level pincode
  roles: string[];
}

export interface RegistrationResponse {
  message: string;
  success: boolean
}
