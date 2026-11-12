import { api } from './client';
import type { components } from './schema';

export type PlateLookup = components['schemas']['PlateLookupResponse'];
export type SpotLookup = components['schemas']['SpotLookupResponse'];
export type VehicleSummary = components['schemas']['VehicleSummary'];

// encodeURIComponent keeps spaces, dashes and anything else the operator types safe inside the URL
export function lookupPlate(propertyId: string, plate: string): Promise<PlateLookup> {
  return api.get<PlateLookup>(`/properties/${propertyId}/lookup/plate?plate=${encodeURIComponent(plate)}`);
}

export function lookupSpot(propertyId: string, label: string): Promise<SpotLookup> {
  return api.get<SpotLookup>(`/properties/${propertyId}/lookup/spot?label=${encodeURIComponent(label)}`);
}