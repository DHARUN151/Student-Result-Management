package servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamCellAnalysisService;
@WebServlet("/ExamCellReportServlet")
public class ExamCellReportServlet extends HttpServlet{
private static final long serialVersionUID=1L;
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
HttpSession session=request.getSession(false);
if(session==null||!"EXAM_CELL".equals(session.getAttribute("role"))){
response.sendRedirect("login.jsp");
return;
}
try{
int departmentId=Integer.parseInt(request.getParameter("departmentId"));
ExamCellAnalysisService service=new ExamCellAnalysisService();
Map<String,Object> analysis=service.getAnalysis(departmentId);
String departmentName=(String)analysis.get("departmentName");
int totalStudents=(Integer)analysis.get("totalStudents");
double mean=(Double)analysis.get("mean");
double standardDeviation=(Double)analysis.get("standardDeviation");
double highest=(Double)analysis.get("highest");
double lowest=(Double)analysis.get("lowest");
int passCount=(Integer)analysis.get("passCount");
int failCount=(Integer)analysis.get("failCount");
double passPercentage=(Double)analysis.get("passPercentage");
int[] distribution=(int[])analysis.get("distribution");
List<Double> percentages=(List<Double>)analysis.get("percentages");
response.setContentType("text/html;charset=UTF-8");
response.setHeader("Content-Disposition","attachment; filename=\""+departmentName.replaceAll("[^a-zA-Z0-9]","_")+"_Result_Analysis_Report.html\"");
PrintWriter out=response.getWriter();
out.println("<!DOCTYPE html>");
out.println("<html>");
out.println("<head>");
out.println("<meta charset='UTF-8'>");
out.println("<title>"+departmentName+" Result Analysis Report</title>");
out.println("</head>");
out.println("<body>");
out.println("<h1 align='center'>STUDENT RESULT ANALYSIS REPORT</h1>");
out.println("<h2 align='center'>"+departmentName+" Department</h2>");
out.println("<hr>");
out.println("<h3>Statistical Summary</h3>");
out.println("<table border='1' cellpadding='8' cellspacing='0' width='100%'>");
out.println("<tr><th>Parameter</th><th>Value</th></tr>");
out.println("<tr><td>Total Students</td><td>"+totalStudents+"</td></tr>");
out.println("<tr><td>Mean</td><td>"+String.format("%.2f",mean)+"%</td></tr>");
out.println("<tr><td>Standard Deviation</td><td>"+String.format("%.2f",standardDeviation)+"</td></tr>");
out.println("<tr><td>Highest</td><td>"+String.format("%.2f",highest)+"%</td></tr>");
out.println("<tr><td>Lowest</td><td>"+String.format("%.2f",lowest)+"%</td></tr>");
out.println("<tr><td>Passed</td><td>"+passCount+"</td></tr>");
out.println("<tr><td>Failed</td><td>"+failCount+"</td></tr>");
out.println("<tr><td>Pass Percentage</td><td>"+String.format("%.2f",passPercentage)+"%</td></tr>");
out.println("</table>");
out.println("<br>");
if(totalStudents==0){
out.println("<h3>Result Analysis</h3>");
out.println("<p>No HOD verified results are currently available for this department.</p>");
}else{
printBellCurve(out,mean,standardDeviation,totalStudents,percentages);
out.println("<br>");
out.println("<h3>Z-Score Performance Classification</h3>");
out.println("<table border='1' cellpadding='8' cellspacing='0' width='100%'>");
out.println("<tr><th>Classification</th><th>Students</th></tr>");
out.println("<tr><td>Very Low</td><td>"+distribution[0]+"</td></tr>");
out.println("<tr><td>Below Average</td><td>"+distribution[1]+"</td></tr>");
out.println("<tr><td>Average</td><td>"+distribution[2]+"</td></tr>");
out.println("<tr><td>Above Average</td><td>"+distribution[3]+"</td></tr>");
out.println("<tr><td>High Performer</td><td>"+distribution[4]+"</td></tr>");
out.println("</table>");
out.println("<br>");
printPerformanceDistribution(out,percentages);
out.println("<br>");
out.println("<h3>Exam Cell Department Approval</h3>");
out.println("<table border='1' cellpadding='8' cellspacing='0' width='100%'>");
out.println("<tr><td><b>Department</b></td><td>"+departmentName+"</td></tr>");
out.println("<tr><td><b>Current Result Status</b></td><td>HOD_VERIFIED</td></tr>");
out.println("<tr><td><b>Approval</b></td><td>Pending Exam Cell Approval</td></tr>");
out.println("</table>");
}
out.println("<br>");
out.println("<hr>");
out.println("<p align='center'><b>Student Result Management System</b></p>");
out.println("<p align='center'>Examination Cell - Result Analysis Report</p>");
out.println("</body>");
out.println("</html>");
}catch(Exception e){
e.printStackTrace();
response.sendRedirect("ExamCellDashboardServlet");
}
}
private void printBellCurve(PrintWriter out,double mean,double standardDeviation,int totalStudents,List<Double> percentages){
out.println("<h3>Bell Curve Analysis</h3>");
out.println("<div align='center'>");
out.println("<svg width='900' height='450' viewBox='0 0 900 450' xmlns='http://www.w3.org/2000/svg'>");
out.println("<text x='450' y='30' text-anchor='middle' font-size='20'>Department Result Distribution and Bell Curve</text>");
out.println("<line x1='70' y1='370' x2='850' y2='370' stroke='black' stroke-width='2'/>");
out.println("<line x1='70' y1='370' x2='70' y2='60' stroke='black' stroke-width='2'/>");
double graphMaximum=100;
double plusTwoSigma=mean+(2*standardDeviation);
if(plusTwoSigma>100){
graphMaximum=Math.ceil(plusTwoSigma/10.0)*10;
}
if(graphMaximum<100){
graphMaximum=100;
}
double[] xValues=new double[101];
double[] curveValues=new double[101];
double maxCurve=0;
for(int i=0;i<=100;i++){
double x=(graphMaximum*i)/100.0;
xValues[i]=x;
double y=0;
if(standardDeviation>0){
y=(1/(standardDeviation*Math.sqrt(2*Math.PI)))*Math.exp(-0.5*Math.pow((x-mean)/standardDeviation,2));
y=y*totalStudents*2;
}
curveValues[i]=y;
if(y>maxCurve){
maxCurve=y;
}
}
double maxActual=0;
for(double value:percentages){
if(value>maxActual){
maxActual=value;
}
}
double maxY=Math.max(maxCurve,1);
if(percentages.size()>0&&maxActual>0){
maxY=Math.max(maxY,1);
}
StringBuilder curvePoints=new StringBuilder();
for(int i=0;i<=100;i++){
double svgX=70+(xValues[i]/graphMaximum)*780;
double svgY=370-(curveValues[i]/maxY)*280;
curvePoints.append(String.format("%.2f,%.2f ",svgX,svgY));
}
out.println("<polyline points='"+curvePoints+"' fill='none' stroke='black' stroke-width='3'/>");
for(double value:percentages){
double svgX=70+(value/graphMaximum)*780;
double svgY=370-8;
out.println("<circle cx='"+String.format("%.2f",svgX)+"' cy='"+String.format("%.2f",svgY)+"' r='5' fill='none' stroke='black' stroke-width='2'/>");
}
out.println("<text x='70' y='395' text-anchor='middle' font-size='13'>0%</text>");
out.println("<text x='850' y='395' text-anchor='middle' font-size='13'>"+String.format("%.0f",graphMaximum)+"%</text>");
out.println("<text x='460' y='425' text-anchor='middle' font-size='15'>Student Percentage</text>");
out.println("<text x='20' y='220' text-anchor='middle' font-size='15' transform='rotate(-90 20 220)'>Number of Students</text>");
out.println("</svg>");
out.println("</div>");
out.println("<p align='center'><b>Student Distribution</b> and <b>Bell Curve</b></p>");
}
private void printPerformanceDistribution(PrintWriter out,List<Double> percentages){
int[] ranges=new int[]{0,0,0,0,0};
for(double value:percentages){
if(value<50){
ranges[0]++;
}else if(value<60){
ranges[1]++;
}else if(value<70){
ranges[2]++;
}else if(value<80){
ranges[3]++;
}else{
ranges[4]++;
}
}
out.println("<h3>Performance Distribution</h3>");
out.println("<table border='1' cellpadding='8' cellspacing='0' width='100%'>");
out.println("<tr><th>Percentage Range</th><th>Number of Students</th></tr>");
out.println("<tr><td>Below 50%</td><td>"+ranges[0]+"</td></tr>");
out.println("<tr><td>50% - 59%</td><td>"+ranges[1]+"</td></tr>");
out.println("<tr><td>60% - 69%</td><td>"+ranges[2]+"</td></tr>");
out.println("<tr><td>70% - 79%</td><td>"+ranges[3]+"</td></tr>");
out.println("<tr><td>80% and Above</td><td>"+ranges[4]+"</td></tr>");
out.println("</table>");
}
}