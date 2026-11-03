package com.lipengzhou.webtvlive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** 当天节目单；当前节目高亮，官方明确支持回看的已播节目标记“回看”。 */
class ProgramGuideAdapter(
    private val onItemClick: (position: Int) -> Unit,
) : RecyclerView.Adapter<ProgramGuideAdapter.VH>() {
    private var items: List<ProgramGuideItem> = emptyList()
    private var channel: Channel? = null
    private var nowEpochSeconds = System.currentTimeMillis() / 1000
    private var columnActive = false

    var selectedIndex = 0
        private set

    class VH(
        root: ViewGroup,
        val time: TextView = root.findViewById(R.id.programTime),
        val name: TextView = root.findViewById(R.id.programName),
        val replayBadge: TextView = root.findViewById(R.id.programReplayBadge),
    ) : RecyclerView.ViewHolder(root)

    fun submit(
        newItems: List<ProgramGuideItem>,
        channel: Channel?,
        now: Long = System.currentTimeMillis() / 1000,
    ): Int {
        items = newItems
        this.channel = channel
        nowEpochSeconds = now
        selectedIndex = currentProgramIndex().takeIf { it >= 0 } ?: 0
        notifyDataSetChanged()
        return selectedIndex
    }

    fun itemAt(position: Int): ProgramGuideItem? = items.getOrNull(position)

    fun updateNow(now: Long = System.currentTimeMillis() / 1000): Int {
        val previousProgram = currentProgramIndex()
        val previousSelection = selectedIndex
        nowEpochSeconds = now
        val current = currentProgramIndex()
        if (!columnActive && current >= 0) selectedIndex = current
        setOf(previousProgram, previousSelection, current, selectedIndex)
            .filter { it in items.indices }
            .forEach(::notifyItemChanged)
        return current
    }

    fun setSelected(index: Int) {
        if (index == selectedIndex || index !in items.indices) return
        val previous = selectedIndex
        selectedIndex = index
        if (previous in items.indices) notifyItemChanged(previous)
        notifyItemChanged(index)
    }

    fun setColumnActive(active: Boolean) {
        if (active == columnActive) return
        columnActive = active
        if (selectedIndex in items.indices) notifyItemChanged(selectedIndex)
    }

    fun currentProgramIndex(): Int =
        items.indexOfFirst { it.isPlayingAt(nowEpochSeconds) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_program_guide, parent, false) as ViewGroup
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val isPlaying = item.isPlayingAt(nowEpochSeconds)
        holder.time.text = item.startTime
        holder.name.text = item.name
        val currentChannel = channel
        holder.replayBadge.visibility = if (
            currentChannel != null &&
            ReplayEligibility.evaluate(currentChannel, item, nowEpochSeconds) ==
            ReplayEligibility.Reason.AVAILABLE
        ) View.VISIBLE else View.GONE
        holder.itemView.isActivated = columnActive && position == selectedIndex
        holder.itemView.isSelected = isPlaying
        holder.itemView.alpha = when {
            isPlaying || position == selectedIndex -> 1f
            item.endEpochSeconds <= nowEpochSeconds -> 0.55f
            else -> 0.82f
        }
        holder.itemView.setOnClickListener { onItemClick(holder.bindingAdapterPosition) }
    }

    override fun getItemCount(): Int = items.size
}
