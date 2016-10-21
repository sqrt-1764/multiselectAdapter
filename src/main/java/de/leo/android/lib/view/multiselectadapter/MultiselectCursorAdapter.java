package de.leo.android.lib.view.multiselectadapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Multiselect adapter for RecyclerViews with cursors as datasource.
 *
 * Created by Matthias Leonhardt on 03.10.16.
 */

public class MultiselectCursorAdapter<BindingObjectFactory extends MultiselectAdapter.DataBindingFactory> extends MultiselectAdapter<BindingObjectFactory> {

    /**
     * Extend the {@link MultiselectAdapter.ActionDataBinding} interface
     * with a method to assign data to the detail-view.
     */
    public interface CursorActionDataBinding extends ActionDataBinding {

        /**
         * Populate the detail-view with data from the cursor.
         * <p>Usually create an data-object from the current position in the cursor and then feeding that
         * object to the databinding responsible for the detail-view.</p>
         *
         * @param context   Optionally internally used by the data-object
         * @param cursor    {@link Cursor} to create the view from.
         */
        void setData(Context context, Cursor cursor);
    }


    public static class MultiselectCursorViewHolder extends MultiselectViewHolder {

        public MultiselectCursorViewHolder(CursorActionDataBinding binding, MultiselectAction selectAction) {
            super(binding.getRoot());

            this.binding = binding;
            this.selectAction = selectAction;
        }


        public void setData(Context context, android.database.Cursor cursor) {
            ((CursorActionDataBinding)binding).setData(context, cursor);
            binding.getRoot().setActivated(selectAction.isActive() && selectAction.getCheckedItems().indexOfKey(getAdapterPosition()) >= 0);
        }
    }


    /**
     * Contains the data that is displayed by this adapter
     */
    private Cursor cursor;


    public MultiselectCursorAdapter(AppCompatActivity activity, BindingObjectFactory factoryObject, Cursor cursor, ActionMode.Callback callback) {
        super(activity, factoryObject, callback);

        this.cursor = cursor;
    }

    @Override
    public void onBindViewHolder(MultiselectViewHolder holder, int position) {
        if (cursor != null) cursor.moveToPosition(position);
        ((MultiselectCursorViewHolder)holder).setData(activity, cursor);
    }

    @Override
    public MultiselectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ActionDataBinding binding = factoryObject.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MultiselectCursorViewHolder((CursorActionDataBinding)binding, selectAction);
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }


    public void swapCursor(Cursor newCursor) {
        this.cursor = newCursor;
        this.notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
            return cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

        } else return RecyclerView.NO_ID;
    }
}
