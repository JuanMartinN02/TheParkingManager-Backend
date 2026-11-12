import { api } from './client';
import type { components } from './schema';

export type Property = components['schemas']['PropertyResponse'];

export function listProperties(): Promise<Property[]> {
  return api.get<Property[]>('/properties');
}

export function getProperty(id: string): Promise<Property> {
  return api.get<Property>(`/properties/${id}`);
}