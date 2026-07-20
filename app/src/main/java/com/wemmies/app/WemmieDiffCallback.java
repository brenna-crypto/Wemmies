package com.wemmies.app;

import androidx.recyclerview.widget.DiffUtil;
import com.wemmies.app.model.Wemmie;
import java.util.List;
import java.util.Objects;

/**
 * WemmieDiffCallback computes the differences between two lists of Wemmies.
 * It is used by DiffUtil to calculate the minimum number of updates required
 * to refresh the RecyclerView list, preventing full-list refreshes (notifyDataSetChanged).
 */
public class WemmieDiffCallback extends DiffUtil.Callback {

    private final List<Wemmie> oldList;
    private final List<Wemmie> newList;

    public WemmieDiffCallback(List<Wemmie> oldList, List<Wemmie> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    /**
     * Determines whether two items represent the same object (e.g. by comparing unique IDs).
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Wemmie oldWemmie = oldList.get(oldItemPosition);
        Wemmie newWemmie = newList.get(newItemPosition);
        return Objects.equals(oldWemmie.getId(), newWemmie.getId());
    }

    /**
     * Determines whether the visual contents of two items are identical.
     * If this returns false, the RecyclerView will re-bind the item in place with animation.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Wemmie oldWemmie = oldList.get(oldItemPosition);
        Wemmie newWemmie = newList.get(newItemPosition);
        
        return oldWemmie.getEmpathyCount() == newWemmie.getEmpathyCount() &&
                oldWemmie.isTransformed() == newWemmie.isTransformed() &&
                Objects.equals(oldWemmie.getShamefulThought(), newWemmie.getShamefulThought()) &&
                Objects.equals(oldWemmie.getEmotionType(), newWemmie.getEmotionType());
    }
}
