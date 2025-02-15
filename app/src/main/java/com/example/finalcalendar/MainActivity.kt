package com.example.finalcalendar

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var fabAddEvent: FloatingActionButton

    private val eventsData = mutableMapOf<String, MutableList<Event>>()
    private val eventDays = mutableListOf<EventDay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerViewEvents)

        fabAddEvent = findViewById(R.id.fabAddEvent)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Définir une couleur par défaut pour l'initialisation
        val defaultColor = ContextCompat.getColor(this, getColorForDayOfWeek(Calendar.MONDAY))

        eventAdapter = EventAdapter(mutableListOf(), defaultColor) { event, position ->
            val intent = Intent(this, EventEditorActivity::class.java)
            intent.putExtra("event", event) // Envoyer l'événement à modifier
            intent.putExtra("position", position) // Envoyer la position dans la liste
            startActivityForResult(intent, 2) // Code 2 pour édition
        }
        recyclerView.adapter = eventAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removedEvent = eventAdapter.getEventAt(position)

                // Supprimer l'événement de la liste
                eventsData[removedEvent.date]?.remove(removedEvent)

                // Si la liste du jour est vide, supprimer aussi l'entrée de eventsData
                if (eventsData[removedEvent.date].isNullOrEmpty()) {
                    eventsData.remove(removedEvent.date)
                }

                // Mettre à jour la liste et le calendrier
                eventAdapter.removeEvent(position)
                updateCalendar()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                val dateKey = getDateKey(calendarDay.calendar)

                // Sélectionner automatiquement le jour cliqué
                calendarView.setDate(calendarDay.calendar.time)

                // Obtenir la couleur du jour sélectionné
                val dayOfWeek = calendarDay.calendar.get(Calendar.DAY_OF_WEEK)
                val eventColor = ContextCompat.getColor(this@MainActivity, getColorForDayOfWeek(dayOfWeek))

                eventAdapter.updateEvents(eventsData[dateKey] ?: mutableListOf(), eventColor)
            }
        })

        fabAddEvent.setOnClickListener {
            val selectedDate = calendarView.selectedDates.firstOrNull() ?: Calendar.getInstance()
            val dateKey = getDateKey(selectedDate)

            val intent = Intent(this, EventEditorActivity::class.java)
            intent.putExtra("selectedDate", dateKey) // Passer la date sélectionnée
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val event = data?.getSerializableExtra("event") as? Event ?: return
            val dateKey = event.date

            // Ajouter l'événement à la liste existante
            val eventList = eventsData.getOrPut(dateKey) { mutableListOf() }
            eventList.add(event)

            // Trier par heure (format "HH:mm")
            eventList.sortBy { it.time }

            // Récupérer la couleur du jour
            val calendar = getCalendarFromKey(dateKey)
            val eventColor = ContextCompat.getColor(this, getColorForDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))

            // Mettre à jour l'adaptateur
            eventAdapter.updateEvents(eventList, eventColor)

            // Mettre à jour le calendrier
            updateCalendar()
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            val updatedEvent = data?.getSerializableExtra("event") as? Event ?: return
            val position = data.getIntExtra("position", -1)

            if (position != -1) {
                eventAdapter.updateEvent(position, updatedEvent)

                // Mettre à jour l'événement dans `eventsData`
                eventsData[updatedEvent.date]?.removeIf { it.time == updatedEvent.time }
                eventsData.getOrPut(updatedEvent.date) { mutableListOf() }.add(updatedEvent)
                eventsData[updatedEvent.date]?.sortBy { it.time }

                updateCalendar()
            }
        }
    }

    private fun getDateKey(calendar: Calendar): String {
        return "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}"
    }

    private fun updateCalendar() {
        eventDays.clear()
        eventsData.forEach { (dateKey, eventList) ->
            val calendar = getCalendarFromKey(dateKey)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            val colorRes = getColorForDayOfWeek(dayOfWeek)

            // Vérifier si l'icône existe avant d'appliquer la couleur
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_event_marker)?.mutate()

            if (drawable != null) {
                drawable.setTint(ContextCompat.getColor(this, colorRes))
                eventDays.add(EventDay(calendar, drawable)) // Appliquer l'icône colorée
            } else {
                Log.e("ERROR", "Drawable introuvable pour ic_event_marker")
            }
        }
        calendarView.setEvents(eventDays)
    }

    private fun getColorForDayOfWeek(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.MONDAY -> R.color.blue
            Calendar.TUESDAY -> R.color.red
            Calendar.WEDNESDAY -> R.color.green
            Calendar.THURSDAY -> R.color.orange
            Calendar.FRIDAY -> R.color.purple
            Calendar.SATURDAY -> R.color.yellow
            Calendar.SUNDAY -> R.color.pink
            else -> R.color.gray
        }
    }

    private fun getCalendarFromKey(key: String): Calendar {
        val parts = key.split("-").map { it.toInt() }
        return Calendar.getInstance().apply {
            set(parts[2], parts[1] - 1, parts[0])
        }
    }
}
