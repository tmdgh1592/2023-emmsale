package com.emmsale.presentation.ui.main.myProfile.itemDecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.emmsale.presentation.utils.extension.dp

class ActivitiesAdapterDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val offset = 13.dp

        if (position != 0) {
            outRect.top = offset
        }
    }
}