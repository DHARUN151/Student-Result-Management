package servlet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import util.DBConnection;
@WebServlet("/addStudent")
public class AddStudentServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);
        if(session==null||!"FACULTY".equals(session.getAttribute("role"))){
            response.sendRedirect("login.jsp");
            return;
        }
        String regNum=request.getParameter("regNum");
        String name=request.getParameter("name");
        String dob=request.getParameter("dob");
        String gender=request.getParameter("gender");
        String phone=request.getParameter("phone");
        String email=request.getParameter("email");
        String doorNo=request.getParameter("doorNo");
        String street=request.getParameter("street");
        String city=request.getParameter("city");
        String district=request.getParameter("district");
        String state=request.getParameter("state");
        String pincode=request.getParameter("pincode");
        String department=request.getParameter("department");
        String admissionYear=request.getParameter("admissionYear");
        String semester=request.getParameter("semester");
        if(regNum==null||name==null||dob==null||gender==null||department==null||admissionYear==null||semester==null){
            response.sendRedirect("addStudent.jsp?error=true");
            return;
        }
        regNum=regNum.trim();
        name=name.trim();
        department=department.trim();
        if(regNum.isEmpty()||name.isEmpty()||dob.isEmpty()||gender.isEmpty()||department.isEmpty()){
            response.sendRedirect("addStudent.jsp?error=true");
            return;
        }
        Connection connection=null;
        try{
            connection=DBConnection.getConnection();
            if(connection==null){
                response.sendRedirect("addStudent.jsp?error=true");
                return;
            }
            connection.setAutoCommit(false);
            int facultyUserId=(Integer)session.getAttribute("userId");
            int studentId=0;
            int programId=0;
            int semesterId=0;
            String checkStudentSql="SELECT student_id FROM enterprise.students WHERE reg_num=?";
            try(PreparedStatement statement=connection.prepareStatement(checkStudentSql)){
                statement.setString(1,regNum);
                try(ResultSet result=statement.executeQuery()){
                    if(result.next()){
                        connection.rollback();
                        response.sendRedirect("addStudent.jsp?error=exists");
                        return;
                    }
                }
            }
            String programSql="SELECT p.program_id FROM enterprise.programs p JOIN enterprise.departments d ON p.department_id=d.department_id WHERE (d.department_code=? OR d.department_name=?) AND p.status='ACTIVE' ORDER BY p.program_id LIMIT 1";
            try(PreparedStatement statement=connection.prepareStatement(programSql)){
                statement.setString(1,department);
                statement.setString(2,department);
                try(ResultSet result=statement.executeQuery()){
                    if(result.next()){
                        programId=result.getInt("program_id");
                    }else{
                        connection.rollback();
                        response.sendRedirect("addStudent.jsp?error=department");
                        return;
                    }
                }
            }
            String semesterSql="SELECT se.semester_id FROM enterprise.semesters se JOIN enterprise.academic_years ay ON se.academic_year_id=ay.academic_year_id WHERE se.program_id=? AND se.semester_number=? AND ay.status='ACTIVE' LIMIT 1";
            try(PreparedStatement statement=connection.prepareStatement(semesterSql)){
                statement.setInt(1,programId);
                statement.setInt(2,Integer.parseInt(semester));
                try(ResultSet result=statement.executeQuery()){
                    if(result.next()){
                        semesterId=result.getInt("semester_id");
                    }else{
                        connection.rollback();
                        response.sendRedirect("addStudent.jsp?error=semester");
                        return;
                    }
                }
            }
            String studentSql="INSERT INTO enterprise.students(reg_num,name,dob,gender,phone,email,account_status) VALUES(?,?,?,?,?,?,?)";
            try(PreparedStatement statement=connection.prepareStatement(studentSql,java.sql.Statement.RETURN_GENERATED_KEYS)){
                statement.setString(1,regNum);
                statement.setString(2,name);
                statement.setDate(3,java.sql.Date.valueOf(dob));
                statement.setString(4,gender);
                statement.setString(5,phone);
                statement.setString(6,email);
                statement.setString(7,"ACTIVE");
                statement.executeUpdate();
                try(ResultSet result=statement.getGeneratedKeys()){
                    if(result.next()){
                        studentId=result.getInt(1);
                    }
                }
            }
            if(studentId==0){
                connection.rollback();
                response.sendRedirect("addStudent.jsp?error=true");
                return;
            }
            String addressSql="INSERT INTO enterprise.student_address(student_id,door_no,street,city,district,state,pincode) VALUES(?,?,?,?,?,?,?)";
            try(PreparedStatement statement=connection.prepareStatement(addressSql)){
                statement.setInt(1,studentId);
                statement.setString(2,doorNo);
                statement.setString(3,street);
                statement.setString(4,city);
                statement.setString(5,district);
                statement.setString(6,state);
                statement.setString(7,pincode);
                statement.executeUpdate();
            }
            String academicSql="INSERT INTO enterprise.academic_details(student_id,program_id,admission_year,current_semester_id) VALUES(?,?,?,?)";
            try(PreparedStatement statement=connection.prepareStatement(academicSql)){
                statement.setInt(1,studentId);
                statement.setInt(2,programId);
                statement.setInt(3,Integer.parseInt(admissionYear));
                statement.setInt(4,semesterId);
                statement.executeUpdate();
            }
            String hashedPassword=BCrypt.hashpw(dob,BCrypt.gensalt());
            String userSql="INSERT INTO enterprise.users(username,password_hash,account_status,student_id,first_login,two_factor_enabled) VALUES(?,?,?,?,false,false)";
            int userId=0;
            try(PreparedStatement statement=connection.prepareStatement(userSql,java.sql.Statement.RETURN_GENERATED_KEYS)){
                statement.setString(1,regNum);
                statement.setString(2,hashedPassword);
                statement.setString(3,"ACTIVE");
                statement.setInt(4,studentId);
                statement.executeUpdate();
                try(ResultSet result=statement.getGeneratedKeys()){
                    if(result.next()){
                        userId=result.getInt(1);
                    }
                }
            }
            if(userId==0){
                connection.rollback();
                response.sendRedirect("addStudent.jsp?error=true");
                return;
            }
            String roleSql="INSERT INTO enterprise.user_roles(user_id,role_id) SELECT ?,role_id FROM enterprise.roles WHERE role_name='STUDENT'";
            try(PreparedStatement statement=connection.prepareStatement(roleSql)){
                statement.setInt(1,userId);
                statement.executeUpdate();
            }
            String auditSql="INSERT INTO enterprise.audit_logs(user_id,action,entity_type,entity_id,new_value,remarks) VALUES(?,?,?,?,?,?)";
            try(PreparedStatement statement=connection.prepareStatement(auditSql)){
                statement.setInt(1,facultyUserId);
                statement.setString(2,"CREATE_STUDENT");
                statement.setString(3,"STUDENT");
                statement.setString(4,String.valueOf(studentId));
                statement.setString(5,"Registration Number: "+regNum);
                statement.setString(6,"Student created by faculty");
                statement.executeUpdate();
            }
            connection.commit();
            response.sendRedirect("addStudent.jsp?success=true");
        }catch(Exception e){
            try{
                if(connection!=null){
                    connection.rollback();
                }
            }catch(Exception rollbackException){
                rollbackException.printStackTrace();
            }
            e.printStackTrace();
            response.sendRedirect("addStudent.jsp?error=true");
        }finally{
            try{
                if(connection!=null){
                    connection.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}