package com.bignerdranch.android.timberworkoutlogs
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity(), WorkoutListFragment.WorkoutListListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) { // Prevent fragment recreation on rotation
            showWorkoutList()
        }
    }

    private fun showWorkoutList() {
        val fragment = WorkoutListFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onWorkoutSelected(workoutId: Long) {
        showWorkoutDetail(workoutId)
    }

    private fun showWorkoutDetail(workoutId: Long) {
        val fragment = WorkoutDetailFragment.newInstance(workoutId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Add to back stack for "back" button navigation
            .commit()
    }
}