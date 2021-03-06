package com.hananabid.commons.adapters

import android.support.v7.view.ActionMode
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback
import com.bignerdranch.android.multiselector.MultiSelector
import com.bignerdranch.android.multiselector.SwappingHolder
import com.hananabid.commons.R
import com.hananabid.commons.activities.BaseSimpleActivity
import com.hananabid.commons.extensions.baseConfig
import com.hananabid.commons.interfaces.MyAdapterListener
import com.hananabid.commons.views.FastScroller
import com.hananabid.commons.views.MyRecyclerView
import java.util.*

abstract class MyRecyclerViewAdapter(val activity: BaseSimpleActivity, val recyclerView: MyRecyclerView, val fastScroller: FastScroller? = null,
                                     val itemClick: (Any) -> Unit) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
    protected val baseConfig = activity.baseConfig
    protected val resources = activity.resources!!
    protected val layoutInflater = activity.layoutInflater
    protected var primaryColor = baseConfig.primaryColor
    protected var textColor = baseConfig.textColor
    protected var backgroundColor = baseConfig.backgroundColor
    protected var itemViews = SparseArray<View>()
    protected val selectedPositions = HashSet<Int>()
    protected var positionOffset = 0

    private val multiSelector = MultiSelector()
    private var actMode: ActionMode? = null
    private var actBarTextView: TextView? = null

    abstract fun getActionMenuId(): Int

    abstract fun prepareItemSelection(view: View)

    abstract fun markItemSelection(select: Boolean, view: View?)

    abstract fun prepareActionMode(menu: Menu)

    abstract fun actionItemPressed(id: Int)

    abstract fun getSelectableItemCount(): Int

    protected fun isOneItemSelected() = selectedPositions.size == 1

    protected fun toggleItemSelection(select: Boolean, pos: Int) {
        if (select) {
            if (itemViews[pos] != null) {
                prepareItemSelection(itemViews[pos])
                selectedPositions.add(pos)
            }
        } else {
            selectedPositions.remove(pos)
        }

        markItemSelection(select, itemViews[pos])

        if (selectedPositions.isEmpty()) {
            finishActMode()
            return
        }

        updateTitle(selectedPositions.size)
    }

    private fun updateTitle(cnt: Int) {
        val selectableItemCount = getSelectableItemCount()
        val selectedCount = Math.min(cnt, selectableItemCount)
        val oldTitle = actBarTextView?.text
        val newTitle = "$selectedCount / $selectableItemCount"
        if (oldTitle != newTitle) {
            actBarTextView?.text = newTitle
            actMode?.invalidate()
        }
    }

    protected fun selectAll() {
        val cnt = itemCount - positionOffset
        for (i in 0 until cnt) {
            selectedPositions.add(i)
            notifyItemChanged(i + positionOffset)
        }
        updateTitle(cnt)
    }

    protected fun setupDragListener(enable: Boolean) {
        if (enable) {
            recyclerView.setupDragListener(object : MyRecyclerView.MyDragListener {
                override fun selectItem(position: Int) {
                    selectItemPosition(position)
                }

                override fun selectRange(initialSelection: Int, lastDraggedIndex: Int, minReached: Int, maxReached: Int) {
                    selectItemRange(initialSelection, lastDraggedIndex - positionOffset, minReached, maxReached)
                }
            })
        } else {
            recyclerView.setupDragListener(null)
        }
    }

    fun setupZoomListener(zoomListener: MyRecyclerView.MyZoomListener?) {
        recyclerView.setupZoomListener(zoomListener)
    }

    fun addVerticalDividers(add: Boolean) {
        if (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }

        if (add) {
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL).apply {
                setDrawable(resources.getDrawable(R.drawable.divider))
                recyclerView.addItemDecoration(this)
            }
        }
    }

    protected fun selectItemPosition(pos: Int) {
        toggleItemSelection(true, pos)
    }

    protected fun selectItemRange(from: Int, to: Int, min: Int, max: Int) {
        if (from == to) {
            (min..max).filter { it != from }.forEach { toggleItemSelection(false, it) }
            return
        }

        if (to < from) {
            for (i in to..from) {
                toggleItemSelection(true, i)
            }

            if (min > -1 && min < to) {
                (min until to).filter { it != from }.forEach { toggleItemSelection(false, it) }
            }

            if (max > -1) {
                for (i in from + 1..max) {
                    toggleItemSelection(false, i)
                }
            }
        } else {
            for (i in from..to) {
                toggleItemSelection(true, i)
            }

            if (max > -1 && max > to) {
                (to + 1..max).filter { it != from }.forEach { toggleItemSelection(false, it) }
            }

            if (min > -1) {
                for (i in min until from) {
                    toggleItemSelection(false, i)
                }
            }
        }
    }

    fun finishActMode() {
        actMode?.finish()
    }

    fun updateTextColor(textColor: Int) {
        this.textColor = textColor
        notifyDataSetChanged()
    }

    fun updatePrimaryColor(primaryColor: Int) {
        this.primaryColor = primaryColor
    }

    fun updateBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    private val adapterListener = object : MyAdapterListener {
        override fun toggleItemSelectionAdapter(select: Boolean, position: Int) {
            toggleItemSelection(select, position)
        }

        override fun getSelectedPositions() = selectedPositions

        override fun itemLongClicked(position: Int) {
            recyclerView.setDragSelectActive(position)
        }
    }

    private val multiSelectorMode = object : ModalMultiSelectorCallback(multiSelector) {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            actionItemPressed(item.itemId)
            return true
        }

        override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
            super.onCreateActionMode(actionMode, menu)
            actMode = actionMode
            actBarTextView = layoutInflater.inflate(R.layout.actionbar_title, null) as TextView
            actMode!!.customView = actBarTextView
            actBarTextView!!.setOnClickListener {
                if (getSelectableItemCount() == selectedPositions.size) {
                    finishActMode()
                } else {
                    selectAll()
                }
            }
            activity.menuInflater.inflate(getActionMenuId(), menu)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu): Boolean {
            prepareActionMode(menu)
            return true
        }

        override fun onDestroyActionMode(actionMode: ActionMode?) {
            super.onDestroyActionMode(actionMode)
            selectedPositions.forEach {
                markItemSelection(false, itemViews[it])
            }
            selectedPositions.clear()
            actBarTextView?.text = ""
            actMode = null
        }
    }

    protected fun createViewHolder(layoutType: Int, parent: ViewGroup?): ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return ViewHolder(view, adapterListener, activity, multiSelectorMode, multiSelector, positionOffset, itemClick)
    }

    protected fun bindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int, view: View) {
        itemViews.put(position, view)
        toggleItemSelection(selectedPositions.contains(position), position)
        holder.itemView.tag = holder
    }

    protected fun removeSelectedItems() {
        selectedPositions.sortedDescending().forEach {
            notifyItemRemoved(it + positionOffset)
            itemViews.put(it, null)
        }

        val newItems = SparseArray<View>()
        (0 until itemViews.size())
                .filter { itemViews[it] != null }
                .forEachIndexed { curIndex, i -> newItems.put(curIndex, itemViews[i]) }

        itemViews = newItems
        finishActMode()
        fastScroller?.measureRecyclerView()
    }

    open class ViewHolder(view: View, val adapterListener: MyAdapterListener? = null, val activity: BaseSimpleActivity? = null,
                          val multiSelectorCallback: ModalMultiSelectorCallback? = null, val multiSelector: MultiSelector,
                          val positionOffset: Int = 0, val itemClick: ((Any) -> (Unit))? = null) : SwappingHolder(view, multiSelector) {
        fun bindView(any: Any, allowLongClick: Boolean = true, callback: (itemView: View, adapterPosition: Int) -> Unit): View {
            return itemView.apply {
                callback(this, adapterPosition)

                if (isClickable) {
                    setOnClickListener { viewClicked(any) }
                    setOnLongClickListener { if (allowLongClick) viewLongClicked() else viewClicked(any); true }
                } else {
                    setOnClickListener(null)
                    setOnLongClickListener(null)
                }
            }
        }

        private fun viewClicked(any: Any) {
            if (multiSelector.isSelectable) {
                val isSelected = adapterListener?.getSelectedPositions()?.contains(adapterPosition - positionOffset) ?: false
                adapterListener?.toggleItemSelectionAdapter(!isSelected, adapterPosition - positionOffset)
            } else {
                itemClick?.invoke(any)
            }
        }

        private fun viewLongClicked() {
            if (!multiSelector.isSelectable && multiSelectorCallback != null) {
                activity?.startSupportActionMode(multiSelectorCallback)
                adapterListener?.toggleItemSelectionAdapter(true, adapterPosition - positionOffset)
            }

            adapterListener?.itemLongClicked(adapterPosition - positionOffset)
        }
    }
}
