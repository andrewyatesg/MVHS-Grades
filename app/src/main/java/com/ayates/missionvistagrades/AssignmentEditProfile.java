package com.ayates.missionvistagrades;

/**
 * Created by ayates on 11/19/16.
 */
public class AssignmentEditProfile
{
    private Assignment a;
    private float originalScore;
    private boolean wasSubmitted;

    public AssignmentEditProfile(Assignment a, float originalScore, boolean wasSubmitted)
    {
        this.a = a;
        this.originalScore = originalScore;
        this.wasSubmitted = wasSubmitted;
    }

    public Assignment getA()
    {
        return a;
    }

    public void setA(Assignment a)
    {
        this.a = a;
    }

    public float getOriginalScore()
    {
        return originalScore;
    }

    public void setOriginalScore(float originalScore)
    {
        this.originalScore = originalScore;
    }

    public boolean isWasSubmitted()
    {
        return wasSubmitted;
    }

    public void setWasSubmitted(boolean wasSubmitted)
    {
        this.wasSubmitted = wasSubmitted;
    }
}
