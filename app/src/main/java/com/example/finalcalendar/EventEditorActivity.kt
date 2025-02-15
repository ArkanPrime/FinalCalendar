package com.example.finalcalendar

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class EventEditorActivity : AppCompatActivity() {

    private lateinit var etEventTitle: EditText
    private lateinit var tvTime: TextView
    private var selectedDate: String = ""
    private var selectedTime: String = "00:00"
    private lateinit var btnSave: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_editor)

        etEventTitle = findViewById(R.id.etEventTitle)
        tvTime = findViewById(R.id.tvEventTime)
        btnSave = findViewById(R.id.btnSave)

        // Récupérer les données
        selectedDate = intent.getStringExtra("selectedDate") ?: ""
        val eventToEdit = intent.getSerializableExtra("event") as? Event
        val position = intent.getIntExtra("position", -1)

        // Si on édite un événement, pré-remplir les champs
        if (eventToEdit != null) {
            etEventTitle.setText(eventToEdit.title)
            tvTime.text = eventToEdit.time
            selectedDate = eventToEdit.date
            selectedTime = eventToEdit.time
        }

        tvTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                tvTime.text = selectedTime
            }, hour, minute, true)

            timePicker.show()
        }

        btnSave.setOnClickListener {
            val title = etEventTitle.text.toString().trim()
            if (title.isEmpty()) return@setOnClickListener
            if (selectedDate.isEmpty()) return@setOnClickListener

            val event = Event(title, selectedDate, selectedTime)
            val resultIntent = Intent().apply {
                putExtra("event", event)
                if (position != -1) putExtra("position", position) // Envoyer la position si on édite
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

}
