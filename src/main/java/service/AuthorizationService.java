package service;

import dao.PermissionDAO;

public class AuthorizationService {
    private PermissionDAO permissionDAO=new PermissionDAO();

    public boolean hasPermission(int userId,String permissionName) {
        if(userId<=0||permissionName==null||permissionName.trim().isEmpty()) {
            return false;
        }
        return permissionDAO.hasPermission(userId,permissionName);
    }
}