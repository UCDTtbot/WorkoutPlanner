package com.shibedays.workoutplanner;

import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;
import com.shibedays.workoutplanner.ui.fragments.TimerFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;


@RunWith(AndroidJUnit4.class)
public class TimerTest {

    @Rule
    public ActivityTestRule<MyWorkoutActivity> activityTestRule = new ActivityTestRule<>(MyWorkoutActivity.class);

    @Rule
    public final ServiceTestRule serviceTestRule = new ServiceTestRule();

    @Before
    public void setup(){
        Workout w = activityTestRule.getActivity().getWrkoutData();
        activityTestRule.getActivity().openTimerFragment(w);
    }

    @Test
    public void runFragment() throws TimeoutException{
        //TODO: Start service?
    }

    @Test
    public void testBoundService() throws TimeoutException {

    }
}
