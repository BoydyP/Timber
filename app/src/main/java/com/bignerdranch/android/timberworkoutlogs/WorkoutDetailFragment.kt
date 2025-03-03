package com.bignerdranch.android.timberworkoutlogs
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutDetailFragment : Fragment() {

    private lateinit var workout: Workout //Should hold current workout

    private lateinit var durationTextView: TextView
    private lateinit var exerciseRecyclerView: RecyclerView
    private var adapter: ExerciseAdapter? = null

    companion object {
        private const val ARG_WORKOUT_ID = "workout_id"

        fun newInstance(workoutId: Long): WorkoutDetailFragment {
            val args = Bundle().apply {
                putLong(ARG_WORKOUT_ID, workoutId)
            }
            return WorkoutDetailFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workoutId = arguments?.getLong(ARG_WORKOUT_ID) ?: -1

        workout = getDummyWorkouts().first { it.id == workoutId } //get workout (replace with db query)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_workout_detail, container, false)

        durationTextView = view.findViewById(R.id.workout_duration_textview)
        exerciseRecyclerView = view.findViewById(R.id.exercise_list_recyclerview)
        exerciseRecyclerView.layoutManager = LinearLayoutManager(context)


        durationTextView.text = "Duration: ${workout.duration} minutes"
        updateUI()

        return view
    }

    private fun updateUI() {
        adapter = ExerciseAdapter(workout.exercises)
        exerciseRecyclerView.adapter = adapter
    }

    private inner class ExerciseHolder(view: View): RecyclerView.ViewHolder(view){
        private lateinit var exercise: Exercise
        private val exerciseNameTextView: TextView = itemView.findViewById(R.id.exercise_name_text_view) // Requires exercise_list_item

        fun bind(exercise: Exercise){
            this.exercise = exercise
            exerciseNameTextView.text = exercise.name
        }
    }

    private inner class ExerciseAdapter(private val exercises: List<Exercise>):
        RecyclerView.Adapter<ExerciseHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseHolder {
            //Requires exercise_list_item.xml layout
            val view = layoutInflater.inflate(R.layout.exercise_list_item, parent, false)
            return ExerciseHolder(view)
        }

        override fun getItemCount() = exercises.size

        override fun onBindViewHolder(holder: ExerciseHolder, position: Int) {
            val exercise = exercises[position]
            holder.bind(exercise)
        }
    }

    private fun getDummyWorkouts(): List<Workout> {
        return listOf(
            Workout(1, 60, mutableListOf(Exercise(1, "Bench Press", mutableListOf(ExerciseSet(1, 100.0, 10))))),
            Workout(2, 45, mutableListOf(Exercise(2, "Squats", mutableListOf(ExerciseSet(2, 140.0, 8))))),
            Workout(3, 75, mutableListOf(Exercise(3, "Deadlifts", mutableListOf(ExerciseSet(3, 180.0, 5)))))
        )
    }
}