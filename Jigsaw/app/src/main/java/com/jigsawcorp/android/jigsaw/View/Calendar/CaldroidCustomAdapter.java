package com.jigsawcorp.android.jigsaw.View.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.jigsawcorp.android.jigsaw.Database.Workout.WorkoutLab;
import com.jigsawcorp.android.jigsaw.Model.Workout;
import com.jigsawcorp.android.jigsaw.R;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class CaldroidCustomAdapter extends CaldroidGridAdapter {
    private Multimap<Long, Workout> mWorkoutsMap = ArrayListMultimap.create();

    public CaldroidCustomAdapter(Context context, int month, int year,
                                 Map<String, Object> caldroidData,
                                 Map<String, Object> extraData) {
        super(context, month, year, caldroidData, extraData);
        Log.i("CaldroidAdapter", "CaldroidCustomAdapter");
        populateWorkoutList();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cellView = convertView;

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.caldroid_custom_cell, null);
        }

        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        TextView dateTextView = (TextView) cellView.findViewById(R.id.caldroid_custom_cell_text_view_date);
        LinearLayout catogryLayout = (LinearLayout) cellView.findViewById(R.id.caldroid_custom_cell_container_category);

        dateTextView.setTextColor(Color.BLACK);

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        Resources resources = context.getResources();

        // Set color of the dates in previous / next month
        if (dateTime.getMonth() != month) {
            dateTextView.setTextColor(resources
                    .getColor(com.caldroid.R.color.caldroid_darker_gray));
        }

        boolean shouldResetDiabledView = false;
        boolean shouldResetSelectedView = false;

        // Customize for disabled dates and date outside min/max dates
        if ((minDateTime != null && dateTime.lt(minDateTime))
                || (maxDateTime != null && dateTime.gt(maxDateTime))
                || (disableDates != null && disableDates.indexOf(dateTime) != -1)) {

            dateTextView.setTextColor(CaldroidFragment.disabledTextColor);
            if (CaldroidFragment.disabledBackgroundDrawable == -1) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.disable_cell);
            } else {
                cellView.setBackgroundResource(CaldroidFragment.disabledBackgroundDrawable);
            }

            if (dateTime.equals(getToday())) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.red_border_gray_bg);
            }

        } else {
            shouldResetDiabledView = true;
        }

        // Customize for selected dates
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            cellView.setBackgroundColor(resources
                    .getColor(com.caldroid.R.color.caldroid_sky_blue));

            dateTextView.setTextColor(Color.BLACK);

        } else {
            shouldResetSelectedView = true;
        }

        if (shouldResetDiabledView && shouldResetSelectedView) {
            // Customize for today
            if (dateTime.equals(getToday())) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.red_border);
            } else {
                cellView.setBackgroundResource(com.caldroid.R.drawable.cell_bg);
            }
        }

        dateTextView.setText("" + dateTime.getDay());
        catogryLayout.removeAllViews();

        if(mWorkoutsMap.containsKey(datetimeList.get(position).getStartOfDay().getMilliseconds(TimeZone.getDefault()) / 1000)) {
            Collection<Workout> workouts = mWorkoutsMap.get(datetimeList.get(position).getStartOfDay().getMilliseconds(TimeZone.getDefault()) / 1000);
            View view = new CategoryIndicatorCircle(context);
            view.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
            catogryLayout.addView(view);
        }

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);

        // Set custom color if required
        setCustomResources(dateTime, cellView, dateTextView);

        List<Workout> workouts = getWorkoutFromDate(dateTime);
        return cellView;
    }

    private List<Workout> getWorkoutFromDate(DateTime date) {
         return WorkoutLab.get(context).getWorkouts(date);
    }

    @Override
    public void setAdapterDateTime(DateTime dateTime) {
        super.setAdapterDateTime(dateTime);
        populateWorkoutList();
    }

    private void populateWorkoutList() {
        List<Workout> workouts = WorkoutLab.get(context).getWorkouts(datetimeList.get(0), datetimeList.get(datetimeList.size() - 1));
        for (int i = 0; i < workouts.size(); ++i) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(workouts.get(i).getStartDate());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);
            calendar.set(year, month, day, 0, 0, 0);
            mWorkoutsMap.put(calendar.getTimeInMillis() / 1000, workouts.get(i));
        }


    }


}
