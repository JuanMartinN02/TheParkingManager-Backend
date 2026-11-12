import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { getProperty, listProperties, type Property } from '../api/properties';
import { ApiError } from '../api/client';
import { useSession } from './SessionContext';

type PropertyState = {
  properties: Property[];
  property: Property | null;
  selectProperty: (id: string) => void;
  loading: boolean;
  error: string | null;
};

const PropertyContext = createContext<PropertyState | null>(null);
const STORAGE_KEY = 'selectedPropertyId';

// Remembers the last property picked on this device. Storage can be blocked, so failures are ignored
function readStoredId(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function storeId(id: string) {
  try {
    localStorage.setItem(STORAGE_KEY, id);
  } catch {
    // Not remembering the choice is fine
  }
}

export function PropertyProvider({ children }: { children: ReactNode }) {
  const { me } = useSession();
  const [properties, setProperties] = useState<Property[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(readStoredId);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const role = me?.role;
  const ownPropertyId = me?.propertyId;

  useEffect(() => {
    // Ignores an answer that arrives after the user changed (e.g. signed out mid-request)
    let cancelled = false;
    setLoading(true);
    setError(null);

    // Operators and super admins work on every property, everyone else only on their own
    const load = role === 'SUPER_ADMIN' || role === 'TOWING_OPERATOR'
      ? listProperties()
      : ownPropertyId
        ? getProperty(ownPropertyId).then((property) => [property])
        : Promise.resolve([]);

    load
      .then((list) => { if (!cancelled) setProperties(list); })
      .catch((err) => { if (!cancelled) setError(err instanceof ApiError ? err.message : "Can't load properties"); })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [role, ownPropertyId]);

  // The remembered property if it's still in the list, otherwise the first one
  const property = properties.find((p) => p.id === selectedId) ?? properties[0] ?? null;

  function selectProperty(id: string) {
    setSelectedId(id);
    storeId(id);
  }

  return (
    <PropertyContext.Provider value={{ properties, property, selectProperty, loading, error }}>
      {children}
    </PropertyContext.Provider>
  );
}

export function useProperty(): PropertyState {
  const state = useContext(PropertyContext);
  if (!state) throw new Error('useProperty must be used inside PropertyProvider');
  return state;
}