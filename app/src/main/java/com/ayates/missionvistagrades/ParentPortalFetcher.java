package com.ayates.missionvistagrades;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ayates on 10/15/16.
 */
public class ParentPortalFetcher
{
    //Error codes
    public static final int INCORRECT_LOGIN = 1;
    public static final int NO_CONNECT = 2;
    public static final int SESSION_TIMEOUT = 3;
    public static final int SESSION_ERROR = 4;
    public static final int NO_STUDENTS = 5;

    private static final String ROOT_URL = "https://parents.vistausd.org/";
    private static final String SESSION_COOKIE_NAME = "ASP.NET_SessionId";
    private static final String AERIES_COOKIE_NAME = "AeriesNet";
    private static final String INCORRECT_PASSWORD_STRING = "The Username and Password entered are incorrect.";

    private String sessionId = null;
    private String aeriesNet = null; //Encodes information about current student selected

    private List<Classroom> classes = new ArrayList<>();
    private List<String> students = new ArrayList<>();

    public ParentPortalFetcher()
    {

    }

    /**
     * Logins the ParentPortalFetcher and retrieves and stores session information
     *
     * @param email    used to login to parent portal
     * @param password used to login to parent portal
     * @return whether login was successful
     */
    public int login(String email, String password)
    {
        /*
        Instantiate session information
         */
        try
        {
            Connection.Response res = Jsoup.connect(ROOT_URL + "LoginParent.aspx").execute();
            sessionId = res.cookie(SESSION_COOKIE_NAME);

            res = Jsoup.connect(ROOT_URL + "LoginParent.aspx")
                    .userAgent("Mozilla")
                    .data("checkMobileDevice", "false", "checkStandaloneMode", "false", "checkTabletDevice", "false", "portalAccountUsername", email, "portalAccountPassword", password)
                    .cookie(SESSION_COOKIE_NAME, sessionId)
                    .method(Connection.Method.POST)
                    .execute();
            aeriesNet = res.cookie(AERIES_COOKIE_NAME);

            if (res.parse().select("span#errorMessage").text().equalsIgnoreCase(INCORRECT_PASSWORD_STRING))
            {
                sessionId = null;
                aeriesNet = null;
                return INCORRECT_LOGIN;
            }
            else if (sessionId == null || aeriesNet == null)
            {
                sessionId = null;
                aeriesNet = null;
                return SESSION_ERROR;
            }

            students = getAllStudents();

            if (students.isEmpty())
            {
                Log.d(LoginPanel.TAG, "When logging in, couldn't find any students so stopping and setting all values to null.");
                sessionId = null;
                aeriesNet = null;
                return NO_STUDENTS;
            }

            int pos = getFirstHighschoolStudent();

            if (pos >= 0)
            {
                int code = changeStudent(pos); //Changes current student to first high school student
                Log.d(LoginPanel.TAG, "Changing student with code " + code + ".");
            }
            else
            {
                Log.d(LoginPanel.TAG, "There was a problem getting the first high school student! No students were found.");
                sessionId = null;
                aeriesNet = null;
                return NO_STUDENTS;
            }

            return 0;

        }
        catch (IOException e)
        {
            return NO_CONNECT;
        }
    }

    /**
     * Resets all classrooms with parent portal information. Undoes all MOCK assignments (but you shouldn't use this method only for that purpose).
     *
     * @return whether refresh was successful
     */
    public int refresh()
    {
        if (sessionId == null || aeriesNet == null)
        {
            Log.d(LoginPanel.TAG, "Session cookies are null during refresh, so stopping.");
            return SESSION_ERROR;
        }

        classes.clear();

        try
        {
            return populateClasses();
        }
        catch (IOException e)
        {
            return NO_CONNECT;
        }
    }

    public Classroom getClassroom(int id)
    {
        for (Classroom classroom : classes)
        {
            if (classroom.getId() == id)
            {
                return classroom;
            }
        }

        return null;
    }

    /**
     * Changes student profile to student at position (pos) on Parent Portal
     *
     * @param pos
     * @return
     */
    public int changeStudent(int pos) //TODO: Handle session timeouts
    {
        try
        {
            Connection conn = Jsoup.connect(ROOT_URL + "default.aspx").
                    cookie(SESSION_COOKIE_NAME, sessionId).
                    cookie(AERIES_COOKIE_NAME, aeriesNet);
            Document doc = conn.get();

            Element e = doc.getElementById("Sub_7");
            if (e == null) return SESSION_TIMEOUT;
            Elements e1 = e.children();
            if (e1.isEmpty()) return SESSION_TIMEOUT;

            String link = ROOT_URL + e1.get(pos).attr("href");
            Connection.Response res = Jsoup.connect(link)
                    .cookie(SESSION_COOKIE_NAME, sessionId)
                    .cookie(AERIES_COOKIE_NAME, aeriesNet)
                    .execute();
            aeriesNet = res.cookie(AERIES_COOKIE_NAME);

            if (aeriesNet == null) return SESSION_TIMEOUT;

            Log.d(LoginPanel.TAG, "Changed student to: " + aeriesNet);
        }
        catch (IOException e)
        {
            return NO_CONNECT;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Log.e(LoginPanel.TAG, "Out of bounds exception in changeStudent", e);
            return NO_STUDENTS;
        }
        catch (NullPointerException e)
        {
            Log.e(LoginPanel.TAG, "Null pointer exception in changeStudent", e);
            return NO_STUDENTS;
        }

        return 0;
    }

    /**
     * Retrieves a list of all students
     * @return
     */
    private List<String> getAllStudents()
    {
        try
        {
            List<String> list = new ArrayList<>();

            Connection conn = Jsoup.connect(ROOT_URL + "default.aspx").
                    cookie(SESSION_COOKIE_NAME, sessionId).
                    cookie(AERIES_COOKIE_NAME, aeriesNet);
            Document doc = conn.get();

            Element e = doc.getElementById("Sub_7");
            Elements e1 = e.children();
            int size = e1.size() - 1; //Subtracting 1 b/c of the "Add New Student" list element which we do not count

            for (int i = 0; i < size; i++)
            {
                Element c = e1.get(i);
                list.add(c.html());
            }

            return list;
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    private int getFirstHighschoolStudent()
    {
        for (int i = 0; i < students.size(); i++)
        {
            if (students.get(i).contains("Grd 9") || students.get(i).contains("Grd 10") || students.get(i).contains("Grd 11") || students.get(i).contains("Grd 12"))
            {
                return i;
            }
        }

        return -1;
    }

    private int populateClasses() throws IOException
    {
        //Unfortunately, your Aeries session has expired due to inactivity...
        Connection conn = Jsoup.connect(ROOT_URL + "GradebookSummary.aspx")
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .cookie(AERIES_COOKIE_NAME, aeriesNet);
        Document gradeBook = conn.get();
        Elements table = gradeBook.select("table#ctl00_MainContent_subGBS_tblEverything");

        if (table.size() <= 0) return SESSION_TIMEOUT;

        int a = 0;
        Elements e = table.select("tr[id*='trPriorTermHeading']");
        e.addAll(table.select("tr[id*='trFutureTermHeading']"));
        if (e.isEmpty())
        {
            a = table.select("tr[id*='ctl00_MainContent_subGBS_DataDetails']").size() - 1;
        }
        else
        {
            a = Integer.parseInt(e.first().id().split("_")[4].substring(3));
        }

        Document defaultPage = Jsoup.connect(ROOT_URL + "GradebookDetails.aspx").cookie(SESSION_COOKIE_NAME, sessionId).cookie(AERIES_COOKIE_NAME, aeriesNet).get();
        Elements ids = defaultPage.select("#ctl00_MainContent_subGBS_dlGN").first().select("option:not(option:contains(<<))");
        //Log.d(LoginPanel.TAG, "Step 1 of this looooooooong process.");

        for (int i = 1; i < a; i++)
        {
            try
            {
                //Log.d(LoginPanel.TAG, "Step 2 - " + rowNum + " - 1 of this looooooooong process.");
                Element row = table.select("#ctl00_MainContent_subGBS_DataDetails_ctl0" + i + "_trGBKItem").first();
                Elements classData = row.getElementsByClass("Data");

                String classTitle = row.select("#ctl00_MainContent_subGBS_DataDetails_ctl0" + i + "_lbtnCourseTitle").first().text();
                int period = Integer.parseInt(classData.get(3).text());
                String teacher = classData.get(4).text();
                float percent = Float.parseFloat(classData.get(5).text());
                String mark = classData.get(7).text();
                String lastUpdated = classData.get(10).text();
                int id = Integer.parseInt(ids.get(i - 1).val().split("_")[0]);
                String term = ids.select("option").get(i - 1).val().split("_")[1];

                //Log.d("Parent Portal", classTitle + " " + period + " " + teacher + " " + percent + " " + mark + " " + lastUpdated + " " + id + " " + term);

                Classroom classroom = new Classroom(classTitle, teacher, period, percent, mark, lastUpdated, id, term); //Classroom object

                Document assignments = Jsoup.connect(ROOT_URL + "Widgets/ClassSummary/RedirectToGradebook?GradebookNumber=" + classroom.getId() + "&Term=" + classroom.getTerm())
                        .cookie(SESSION_COOKIE_NAME, sessionId).cookie(AERIES_COOKIE_NAME, aeriesNet).get(); //Scraping class assignment page from parent portal
                boolean classWeighted = assignments.select("td[id*='tdPctOfGrade']").size() > 0;

                if (classWeighted)
                {
                    int d = assignments.select("td[id*='ctl00_MainContent_subGBS_DataSummary']").select("td[id*='dPTS']").size() - 1;
                    for (int j = 1; j <= d; j++)
                    {
                        //Log.d(LoginPanel.TAG, "Step 2 - " + rowNum + " - " + (3 + i) + " of this looooooooong process.");
                        String catName = assignments.select("td#ctl00_MainContent_subGBS_DataSummary_ctl" + (j > 9 ? "" : "0") + j + "_tdDESC").text();
                        float weight = Float.parseFloat(assignments.select("td#ctl00_MainContent_subGBS_DataSummary_ctl" + (j > 9 ? "" : "0") + j + "_tdPctOfGrade").text().replace('%', ' '));
                        classroom.addCategory(catName, weight);
                    }
                }

                classes.add(classroom);

                populateAssignments(classroom, assignments);
            }
            catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e1)
            {
                Log.e(LoginPanel.TAG, "I got an error adding a classroom", e1);
            }

            //System.out.println(classTitle + ": Period " + period + ": Teacher " + teacher + ": Percent " + percent + ": Grade " + mark + ": Last Updated " + lastUpdated + ": ID " + id + ": Term " + term);
            //System.out.println();
        }

        return 0;
    }

    private int populateAssignments(Classroom classroom, Document d) throws IOException
    {

        Elements list = d.getElementsByClass("assignment-info");

        for (Element e : list) //Loop through each assignment (for each class)
        {
            Elements plainData = e.getElementsByClass("PlainDataClear");

            String name = plainData.get(1).text();
            String category = plainData.get(3).text();
            float maxScore = Float.parseFloat(plainData.get(4).select("td").last().text());
            boolean isSubmitted = false;

            String scoreStr = plainData.get(4).select("tr").first().select("td").first().text();
            String percentStr = plainData.get(6).text().replace('%', ' ');
            float score = 0f;
            float percentage = 0f;

            if (!percentStr.isEmpty()) isSubmitted = true;
            if (isSubmitted)
            {
                try
                {
                    score = Float.parseFloat(scoreStr);
                }
                catch (NumberFormatException e1)
                {
                    score = 0f;
                }
                ;
                //Log.d("Parent Portal", classroom.getName() + " " + name);
                percentage = Float.parseFloat(percentStr);
            }

            classroom.addAssignment(new Assignment(name, category, score, maxScore, percentage, isSubmitted));

            //System.out.println(name + " : " + category + " : " + score + " : " + maxScore + " : " + percentage);
        }

        classroom.recalculateGrades();


        return 0;
    }

    public List<Classroom> getClasses()
    {
        return classes;
    }

    public List<String> getStudents()
    {
        return students;
    }

    /**
     * Calculates the mark off of percentage points
     *
     * @param percent
     * @return
     */
    static String getMark(float percent)
    {
        if (percent <= 59.999f) return "F";
        if (percent <= 69.999f) return "D";
        if (percent <= 79.999f) return "C";
        if (percent <= 89.999f) return "B";
        return "A";
    }
}
