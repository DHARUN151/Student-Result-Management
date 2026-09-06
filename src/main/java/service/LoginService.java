package service;
import org.mindrot.jbcrypt.BCrypt;
import dao.UserDAO;
import model.User;
public class LoginService{
    private UserDAO userDAO;
    public LoginService(){
        userDAO=new UserDAO();
    }
    public User login(String username,String password){
        if(username==null||username.trim().isEmpty()){
            return null;
        }
        if(password==null||password.isEmpty()){
            return null;
        }
        User user=userDAO.findUserByUsername(username.trim());
        if(user==null){
            return null;
        }
        if(!"ACTIVE".equals(user.getAccountStatus())){
            return null;
        }
        if(!BCrypt.checkpw(password,user.getPasswordHash())){
            return null;
        }
        return user;
    }
}