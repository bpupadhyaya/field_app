export interface RoleAccess {
  canViewSuperAdmin: boolean
  canViewAdmin: boolean
  canViewManager: boolean
  canViewUser: boolean
}

export function deriveRoleAccess(roles: string[]): RoleAccess {
  const roleSet = new Set(roles || [])
  const canViewSuperAdmin = roleSet.has('ROLE_SUPER_ADMIN')
  const canViewAdmin = canViewSuperAdmin || (roleSet.has('ROLE_ADMIN') && !roleSet.has('ROLE_MANAGER'))
  const canViewManager = !canViewSuperAdmin && roleSet.has('ROLE_MANAGER')
  const canViewUser = !canViewSuperAdmin && !roleSet.has('ROLE_MANAGER') && roleSet.has('ROLE_USER')

  return { canViewSuperAdmin, canViewAdmin, canViewManager, canViewUser }
}
