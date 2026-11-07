package com.zbob.security;

import com.zbob.modules.users.entity.Role;

/** The principal placed in the security context for a verified token. */
public record AuthenticatedUser(Long id, Role role) {
}
