package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import dao.AdminDAO;

public class AdminService {
    private AdminDAO dao=new AdminDAO();

    public boolean authenticateAdmin(String username,String password) {
        if(username==null||password==null) {
            return false;
        }

        username=username.trim();

        if(username.isEmpty()||password.isEmpty()) {
            return false;
        }

        return dao.authenticateAdmin(username,password);
    }

    public int getAdminUserId(String username) {
        if(username==null||username.trim().isEmpty()) {
            return 0;
        }

        return dao.getAdminUserId(username.trim());
    }

    public String getAdminRole(String username) {
        if(username==null||username.trim().isEmpty()) {
            return null;
        }

        return dao.getAdminRole(username.trim());
    }

    public boolean isAdminRole(String role) {
        return "ADMIN".equals(role)||"SUPER_ADMIN".equals(role);
    }

    public boolean isAllowedRole(String role) {
        if(role==null) {
            return false;
        }

        return "FACULTY".equals(role)||"HOD".equals(role)||"EXAM_CELL".equals(role);
    }

    public boolean createAccount(String employeeCode,String name,String email,String phone,String departmentId,String designation,String password,String role,int adminUserId) {
        if(!isAllowedRole(role)) {
            return false;
        }

        if(employeeCode==null||name==null||password==null) {
            return false;
        }

        employeeCode=employeeCode.trim();
        name=name.trim();

        if(employeeCode.isEmpty()||name.isEmpty()||password.isEmpty()) {
            return false;
        }

        if(("FACULTY".equals(role)||"HOD".equals(role))&&(departmentId==null||departmentId.trim().isEmpty())) {
            return false;
        }

        return dao.createAccount(employeeCode,name,email,phone,departmentId,designation,password,role,adminUserId);
    }

    public List<Map<String,Object>> getManagedAccounts(int page,int pageSize) {
        if(page<1) {
            page=1;
        }

        if(pageSize!=10&&pageSize!=20&&pageSize!=50) {
            pageSize=20;
        }

        return dao.getManagedAccounts(page,pageSize);
    }

    public int getManagedAccountsCount() {
        return dao.getManagedAccountsCount();
    }

    public Map<String,Object> getManagedAccount(int userId) throws SQLException {
        if(userId<=0) {
            return null;
        }

        return dao.getManagedAccount(userId);
    }

    public boolean deactivateAccount(int targetUserId,int adminUserId) {
        if(targetUserId<=0||adminUserId<=0||targetUserId==adminUserId) {
            return false;
        }

        return dao.deactivateAccount(targetUserId,adminUserId);
    }
}