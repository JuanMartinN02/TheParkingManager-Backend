// TEMPORARY stand-in so the project compiles before the backend is running.
// `npm run gen:api` overwrites this whole file with the real types from the API
export interface components {
  schemas: {
    UserResponse: {
      /** Format: uuid */
      id?: string;
      email?: string;
      /** @enum {string} */
      role?: 'SUPER_ADMIN' | 'PROPERTY_ADMIN' | 'RESIDENT' | 'TOWING_OPERATOR';
      /** Format: uuid */
      propertyId?: string;
      /** Format: uuid */
      apartmentId?: string;
      active?: boolean;
      mustChangePassword?: boolean;
    };
  };
}