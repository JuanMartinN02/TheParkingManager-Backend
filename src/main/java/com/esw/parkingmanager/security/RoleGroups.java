package com.esw.parkingmanager.security;

import com.esw.parkingmanager.model.Role;

import java.util.Set;

// lets me write SUPER_ADMIN instead of Role.SUPER_ADMIN. More readable.
import static com.esw.parkingmanager.model.Role.*;

// Named role groups so every endpoint says who can use it in plain words
public final class RoleGroups {

    /* Set.of(...) creates an immutable set. No code can add a role to ADMINS at runtime by accident.
    A plain HashSet here would be a shared, changeable global, which is a classic bug source. */

    public static final Set<Role> SUPER_ADMIN_ONLY = Set.of(SUPER_ADMIN);

    // Operators need the property list to pick where they're working
    public static final Set<Role> SUPER_ADMIN_AND_OPERATOR = Set.of(SUPER_ADMIN, TOWING_OPERATOR);

    // Manage a property's data: apartments, spots, authorizations
    public static final Set<Role> ADMINS = Set.of(SUPER_ADMIN, PROPERTY_ADMIN);

    // Read a property's data and run lookups
    public static final Set<Role> STAFF = Set.of(SUPER_ADMIN, PROPERTY_ADMIN, TOWING_OPERATOR);

    // Register and remove cars. Residents do this for their own apartment
    public static final Set<Role> VEHICLE_EDITORS = Set.of(SUPER_ADMIN, PROPERTY_ADMIN, RESIDENT);

    // Still limited by property and apartment in AccessGuard
    public static final Set<Role> EVERYONE = Set.of(Role.values());

    private RoleGroups() {
    }
}