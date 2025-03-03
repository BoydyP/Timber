package com.bignerdranch.android.timberworkoutlogs
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var adapter: WorkoutAdapter? = null
    private var workoutListListener : WorkoutListListener? = null

    // Interface for communication with com.bignerdranch.android.timberworkoutlogs.MainActivity
    interface WorkoutListListener {
        fun onWorkoutSelected(workoutId: Long)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        workoutListListener = context as WorkoutListListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout_list, container, false)
        recyclerView = view.findViewById(R.id.workout_list_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Load and update the workout data (dummy data for now)
        updateUI(getDummyWorkouts())

        return view
    }

    private fun updateUI(workouts: List<Workout>) {
        adapter = WorkoutAdapter(workouts)
        recyclerView.adapter = adapter
    }

    private inner class WorkoutHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var workout: Workout
        private val durationTextView: TextView = itemView.findViewById(R.id.workout_duration_list_item)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(workout: Workout) {
            this.workout = workout
            durationTextView.text = "Duration: ${workout.duration} minutes"
        }

        override fun onClick(v: View?) {
            workoutListListener?.onWorkoutSelected(workout.id)
        }
    }

    private inner class WorkoutAdapter(private val workouts: List<Workout>) :
        RecyclerView.Adapter<WorkoutHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutHolder {
            val view = layoutInflater.inflate(R.layout.list_item_workout, parent, false)
            return WorkoutHolder(view)
        }

        override fun onBindViewHolder(holder: WorkoutHolder, position: Int) {
            val workout = workouts[position]
            holder.bind(workout)
        }

        override fun getItemCount() = workouts.size
    }


    // Dummy data for initial testing
    private fun getDummyWorkouts(): List<Workout> {
        return listOf(
            Workout(1, 60, mutableListOf(Exercise(1, "Bench Press", mutableListOf(
                ExerciseSet(1, 100.0, 10)
            )))),
            Workout(2, 45, mutableListOf(Exercise(2, "Squats", mutableListOf(ExerciseSet(2, 140.0, 8))))),
            Workout(3, 75, mutableListOf(Exercise(3, "Deadlifts", mutableListOf(ExerciseSet(3, 180.0, 5)))))
        )
    }

    override fun onDetach() {
        super.onDetach()
        workoutListListener = null
    }
}