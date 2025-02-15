package com.example.finalcalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class EventAdapter(private var events: MutableList<Event>,
                   private var eventColor: Int,
                   private val onEventEdit: (Event, Int) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvTime: TextView = view.findViewById(R.id.tvEventTime)
        val container: MaterialCardView = view.findViewById(R.id.eventCard) // Correction ici
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.tvTitle.text = event.title
        holder.tvTime.text = event.time

        // Appliquer la couleur seulement au premier événement
        if (position == 0) {
            (holder.container as com.google.android.material.card.MaterialCardView).setCardBackgroundColor(eventColor)
        } else {
            (holder.container as com.google.android.material.card.MaterialCardView).setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }

        holder.itemView.setOnLongClickListener {
            onEventEdit(event, position)
            true
        }
    }



    override fun getItemCount() = events.size

    fun getEventAt(position: Int): Event {
        return events[position]
    }

    fun removeEvent(position: Int) {
        events.removeAt(position)
        notifyItemRemoved(position)

        // Vérifier s'il reste des événements et changer la couleur du premier
        if (events.isNotEmpty()) {
            notifyItemChanged(0) // Rafraîchir le premier élément pour mettre à jour sa couleur
        }
    }

    fun updateEvent(position: Int, updatedEvent: Event) {
        events[position] = updatedEvent
        notifyItemChanged(position)
    }


    fun updateEvents(newEvents: List<Event>, color: Int) {
        events.clear()
        events.addAll(newEvents)
        eventColor = color // Met à jour la couleur pour le premier événement
        notifyDataSetChanged()
    }
}
