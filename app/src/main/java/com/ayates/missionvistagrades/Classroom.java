package com.ayates.missionvistagrades;

import android.util.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ayates on 10/15/16.
 */
public class Classroom
{
    private String name;
    private String teacher;
    private int period;
    private float percent;
    private String mark;
    private String lastUpdated;
    private int id;
    private String term;
    private List<Assignment> assignmentList = new ArrayList<>();
    private HashMap<String, Float> categories = new HashMap<String, Float>();

    Classroom(String name, String teacher, int period, float percent, String mark, String lastUpdated, int id, String term)
    {

        this.name = name;
        this.teacher = teacher;
        this.period = period;
        this.percent = percent;
        this.mark = mark;
        this.lastUpdated = lastUpdated;
        this.id = id;
        this.term = term;
    }

    public String getName()
    {
        return name;
    }

    public String getTeacher()
    {
        return teacher;
    }

    public int getPeriod()
    {
        return period;
    }

    public float getPercent()
    {
        return percent;
    }

    public String getMark()
    {
        return mark;
    }

    public String getLastUpdated()
    {
        return lastUpdated;
    }

    public int getId()
    {
        return id;
    }

    public String getTerm()
    {
        return term;
    }

    public float getTotalPoints()
    {
        float tot = 0f;

        for (Assignment a : assignmentList)
        {
            if (!a.isSubmitted()) continue;
            tot += a.getScore();
        }

        return tot;
    }

    public float getTotalPoints(String cat)
    {
        float tot = 0f;

        for (Assignment a : assignmentList)
        {
            if (!a.isSubmitted()) continue;
            if (a.getCategory().equalsIgnoreCase(cat)) tot += a.getScore();
        }

        return tot;
    }

    public float getTotalMaxPoints()
    {
        float tot = 0f;

        for (Assignment a : assignmentList)
        {
            if (!a.isSubmitted()) continue;
            tot += a.getMaxScore();
        }

        return tot;
    }

    public float getTotalMaxPoints(String cat)
    {
        float tot = 0f;

        for (Assignment a : assignmentList)
        {
            if (!a.isSubmitted()) continue;
            if (a.getCategory().equalsIgnoreCase(cat)) tot += a.getMaxScore();
        }

        return tot;
    }

    /**
     * Does not return actual assignmentList object, just a copy
     *
     * @return
     */
    public List<Assignment> getAssignmentList()
    {
        return assignmentList;
    }

    /**
     * Does not return actual categories object, just a copy
     *
     * @return
     */
    public HashMap<String, Float> getCategoriesMap()
    {
        return categories;
    }

    /**
     * This can be used to add mock assignments. Just make sure to call recalculateGrades afterwords.
     *
     * @param assignment
     */
    public void addAssignment(Assignment assignment)
    {
        assignmentList.add(assignment);
    }

    public void addCategory(String name, float weight)
    {
        categories.put(name, weight);
    }

    /**
     * Make sure to call this after adding new assignment
     * <p>
     * FORMULA: (# points in first category) / (# points possible in first category) * (weight of first category)
     */
    public void recalculateGrades()
    {
        Log.d(LoginPanel.TAG, "Recalculating grades for " + name + " and found " + assignmentList.size() + " assignments.");

        float weightedPoints = 0f;
        float totWeight = 0f;

        if (!categories.isEmpty())
        {
            List<String> usedCats = getUsedCategories();
            //Log.d("Parent Portal", "used categories: " + usedCats.size() + " and actual categories: " + categories.size());

            for (String s : usedCats)
            {
                float points = getTotalPoints(s);
                float maxPoints = getTotalMaxPoints(s);
                float weight = categories.get(s);

                //Log.d("Parent Portal", points + " / " + maxPoints + " for " + s);
                weightedPoints += points / maxPoints * weight;
                totWeight += weight;
            }

            if (totWeight == 0f)
            {
                this.percent = 100f;
                return;
            }

            this.percent = roundFloat(weightedPoints / totWeight * 100f);
            this.mark = ParentPortalFetcher.getMark(this.percent);
        }
        else
        {
            weightedPoints += getTotalPoints();
            totWeight += getTotalMaxPoints();

            if (totWeight == 0f)
            {
                this.percent = 100f;
                return;
            }

            this.percent = roundFloat(weightedPoints / totWeight * 100f);
            this.mark = ParentPortalFetcher.getMark(this.percent);
        }
    }

    private List<String> getUsedCategories()
    {
        List<String> used = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < categories.size(); i++)
        {
            String name = (String) categories.keySet().toArray()[i];
            if (getTotalMaxPoints(name) != 0) used.add(name);
        }

        return used;
    }

    private float roundFloat(float x)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return Float.valueOf(df.format(x));
    }
}
