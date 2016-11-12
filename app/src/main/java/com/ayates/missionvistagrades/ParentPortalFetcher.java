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

    private static final String SESSION_COOKIE_NAME = "ASP.NET_SessionId";
    private static final String AERIES_COOKIE_NAME = "AeriesNet";
    private static final String INCORRECT_PASSWORD_STRING = "The Username and Password entered are incorrect.";

    private String sessionId = null;
    private String aeriesNet = null;

    private List<Classroom> classes = new ArrayList<>();

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
            Connection.Response res = Jsoup.connect("https://parents.vistausd.org").execute();
            sessionId = res.cookie(SESSION_COOKIE_NAME);

            res = Jsoup.connect("https://parents.vistausd.org/LoginParent.aspx").data("checkCookiesEnabled", "true", "checkMobileDevice", "false", "checkStandaloneMode", "false",
                    "portalAccountUsername", email, "portalAccountPassword", password).cookie(SESSION_COOKIE_NAME, sessionId).method(Connection.Method.POST).execute();
            aeriesNet = res.cookie(AERIES_COOKIE_NAME);

            Log.d(LoginPanel.TAG, "Just logged in with sessionId=" + sessionId + " & aeriesNet=" + aeriesNet);


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

            return 0;

        }
        catch (IOException e)
        {
            return NO_CONNECT;
        }
    }

    /**
     * Resets all classrooms with parent portal information. Undoes all MOCK assignments.
     *
     * @return whether refresh was successful
     */
    public int refresh()
    {
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

    private int populateClasses() throws IOException
    {
        //Unfortunately, your Aeries session has expired due to inactivity...
        Connection conn = Jsoup.connect("https://parents.vistausd.org/GradebookSummary.aspx").
                cookie(SESSION_COOKIE_NAME, sessionId).
                cookie(AERIES_COOKIE_NAME, aeriesNet);
        Document gradeBook = conn.get();
        Elements table = gradeBook.select("table#ctl00_MainContent_subGBS_tblEverything");

        if (table.size() <= 0) return SESSION_TIMEOUT;

        Element e = table.select("tr[id*='trPriorTermHeading']").first();
        int rowNum = Integer.parseInt(e.id().split("_")[4].substring(3));

        Document defaultPage = Jsoup.connect("https://parents.vistausd.org/GradebookDetails.aspx").cookie(SESSION_COOKIE_NAME, sessionId).cookie(AERIES_COOKIE_NAME, aeriesNet).get();
        Elements ids = defaultPage.select("#ctl00_MainContent_subGBS_dlGN").first().select("option:not(option:contains(<<))");
        //Log.d(LoginPanel.TAG, "Step 1 of this looooooooong process.");

        for (int i = 1; i < rowNum; i++)
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

                Document assignments = Jsoup.connect("https://parents.vistausd.org/Widgets/ClassSummary/RedirectToGradebook?GradebookNumber=" + classroom.getId() + "&Term=" + classroom.getTerm())
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

                populateAssignments(classroom, assignments);

                classes.add(classroom);
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
