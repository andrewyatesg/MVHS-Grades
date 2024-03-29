package com.ayates.missionvistagrades;

/**
 * Created by ayates on 10/16/16.
 */
public class Assignment
{
    private String name;
    private String category;
    private float score;
    private float maxScore;
    private float percentage;
    private boolean submitted;

    private boolean isEditing;

    public Assignment(String name, String category, float score, float maxScore, float percentage, boolean submitted)
    {
        this.name = name;
        this.category = category;
        this.score = score;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.submitted = submitted;
        this.isEditing = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
        this.percentage = Classroom.roundFloat(score / maxScore * 100);
    }

    public float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public boolean isEditing()
    {
        return isEditing;
    }

    public void setEditing(boolean editing)
    {
        isEditing = editing;
    }

    public void setSubmitted(boolean submitted)
    {
        this.submitted = submitted;
    }
}
