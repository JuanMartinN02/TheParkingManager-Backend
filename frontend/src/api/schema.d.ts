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
    PropertyResponse: {
      /** Format: uuid */
      id?: string;
      name?: string;
      /** Format: int32 */
      visitorSpotCapacity?: number;
      /** Format: int32 */
      permitSpotCapacity?: number;
      /** Format: int32 */
      visitorPassDurationHours?: number;
      mapPdfUrl?: string;
    };
    VehicleSummary: {
      plate?: string;
      make?: string;
      model?: string;
      /** Format: int32 */
      year?: number;
      color?: string;
    };
    AuthorizationSummary: {
      spotLabel?: string;
      /** Format: date */
      validUntil?: string;
    };
    PlateLookupResponse: {
      /** @enum {string} */
      status?: 'RESIDENT_AUTHORIZED' | 'RESIDENT_REGISTERED' | 'UNKNOWN';
      plate?: string;
      unitNumber?: string;
      vehicle?: components['schemas']['VehicleSummary'];
      authorizations?: components['schemas']['AuthorizationSummary'][];
      /** Format: date-time */
      checkedAt?: string;
    };
    SpotLookupResponse: {
      spotLabel?: string;
      assigned?: boolean;
      unitNumber?: string;
      /** Format: date */
      validUntil?: string;
      vehicles?: components['schemas']['VehicleSummary'][];
      /** Format: date-time */
      checkedAt?: string;
    };
  };
}