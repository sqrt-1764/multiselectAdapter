package de.leo.android.lib.view.multiselectadapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


/**
 * {@link RecyclerView.Adapter} supporting MultiSelect with ActionMode. The detail-views are created by the
 * {@link android.databinding.ViewDataBinding ViewDataBinding} library.
 *
 * <p>The a factory object is passed to the constructor which creates the detail-views using the embedded
 * {@link android.databinding.ViewDataBinding ViewDataBinding} class that was created by AndroidStudio.<br />
 * The generated object also provides an interface to communicate with that databinding in an abstract
 * way (Setting content-data, defining an action for short clicks in the detail-view (if not in Multi-
 * Select Mode)</p>
 *
 * Created by Matthias Leonhardt on 09.06.16.
 *
 *
 * @param <BindingObjectFactory>
 */
public abstract class MultiselectAdapter<BindingObjectFactory extends MultiselectAdapter.DataBindingFactory>
                extends RecyclerView.Adapter<MultiselectAdapter.MultiselectViewHolder> {

    /**
     * Interface methods to handle the transition to a detail-view for the selected item.
     */
    public interface ActionDataBinding<ItemType> {

        /**
         * @return  Rootview of the View-Structure of this detail-view. Usually the {@code getRoot()} of the
         *          underlying databinding-object.
         */
        View getRoot();

        /**
         * Action that is executed when the detail-view is short clicked unless the Recycler-View
         * is in MultiSelect-Mode.
         *
         * @param context   Context is needed to create the intent to call the detail-activity.
         * @param item      Either the object that shall be handled by the detail-view or a key
         *                  into a database of the app identifying that object.
         */
        void editDetails(Context context, ItemType item);

        Object getItem();
    }


    /**
     * Interface for a factory that creates {@link android.databinding.ViewDataBinding ViewDataBinding}
     * that implements the {@link ActionDataBinding} interface.
     */
    public interface DataBindingFactory {
        /**
         * Make the static inflate-Method of the underlying databinding-object accessible
         * to the runtime environment.
         */
        ActionDataBinding inflate(LayoutInflater inflater, ViewGroup viewGroup, boolean attachToRoot);
    }


    /**
     * ViewHolder using {@link android.databinding.ViewDataBinding ViewDataBinding}
     * <p>The ViewHolder is responsible for consuming the click-events (long or short)<br />
     * On long-click the MultiChoice-Mode is to be activated.<br />
     * The behaviour in short-clicks depends on the choice-mode. If Multi-Choice is active, a click
     * toggles the selected state of the clicked row.<br />
     * Otherwise an action to edit the details is started.</p>
     */
    public abstract static class MultiselectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected ActionDataBinding binding;
        protected MultiselectAction selectAction;

        protected MultiselectViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (selectAction != null && selectAction.isActive) {
                int adapterPosition = getAdapterPosition();
                setActivated(selectAction.toggleSelection(adapterPosition));

                if (selectAction.checkedItems.size() == 0) selectAction.startActionMode(false);
            } else {
                binding.editDetails(v.getContext(), binding.getItem());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (selectAction != null) {
                selectAction.startActionMode(true);     // Activate MultiChoice-Mode
                onClick(v);
            }
            return true;
        }


        public void setActivated(boolean activated) {
            binding.getRoot().setActivated(activated);
        }
    }


    /**
     * Handle actions on multiple selected items in the adapter
     */
    protected static class MultiselectAction {
        private ActionMode actionMode;
        private MultiselectAdapter adapter;
        private ActionMode.Callback callback;
        private AppCompatActivity activity;

        private boolean isActive = false;

        private SparseBooleanArray checkedItems = new SparseBooleanArray();


        public MultiselectAction(MultiselectAdapter adapter, AppCompatActivity activity, ActionMode.Callback callback) {
            this.activity = activity;
            this.adapter = adapter;
            this.callback = callback;
        }


        /**
         * Activate or deactivate the action mode of the corresponding {@link MultiselectAdapter}.
         */
        public void startActionMode(boolean activate) {
            if (callback == null) {
                isActive = false;
                return;
            }

            if (activate) {
                actionMode = activity.startSupportActionMode(new ActionMode.Callback() {

                        // Forward all methods to the external callback except for onDestroyActionmode for internal cleanup
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return callback.onCreateActionMode(mode, menu);
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return callback.onPrepareActionMode(mode, menu);
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return callback.onActionItemClicked(mode, item);
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        checkedItems.clear();
                        adapter.notifyDataSetChanged();

                        isActive = false;

                        callback.onDestroyActionMode(mode);
                    }
                });

            } else {
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            }

            isActive = activate;
        }


        public boolean isActive() { return isActive; }


        public SparseBooleanArray getCheckedItems() { return checkedItems; }


        public void setItemChecked(int adapterPosition, boolean checked) {
            if (checked)
                checkedItems.put(adapterPosition, true);
            else {
                if (checkedItems.indexOfKey(adapterPosition) >= 0)
                    checkedItems.delete(adapterPosition);
            }
            adapter.notifyItemChanged(adapterPosition);
        }


        public boolean toggleSelection(int adapterPosition) {
            boolean newActivatedState;
            if (checkedItems.indexOfKey(adapterPosition) >= 0) {
                checkedItems.delete(adapterPosition);
                newActivatedState = false;
            } else {
                checkedItems.put(adapterPosition, true);
                newActivatedState = true;
            }

            return newActivatedState;
        }
    }


    /**
     * Needed to start the actionmode otherwise a simle {@link Context} would have been sufficient
     */
    protected AppCompatActivity activity;

    /**
     *
     */
    protected MultiselectAction selectAction;

    /**
     * Factory to create the databinding-objects for new ViewHolders
     */
    protected BindingObjectFactory factoryObject;


    /**
     * Constructor for a new {@link MultiselectAdapter}-Object.
     *
     * <p>Implements the actionmode where muliple items can be selected and operations on the selected
     * items can be scheduled.</p>
     *
     * @param activity      Activity that holds the actionmode. The context is also used by the event-class
     * @param factoryObject
     * @param callback      Callback-Interface for the action-mode
     */
    public MultiselectAdapter(AppCompatActivity activity, BindingObjectFactory factoryObject, ActionMode.Callback callback) {
        this.activity = activity;
        this.factoryObject = factoryObject;

        this.selectAction = (callback == null) ? null : new MultiselectAction(this, activity, callback);
        setHasStableIds(true);
    }


    public void setItemChecked(int adapterPosition, boolean checked) {
        if (selectAction == null)
            throw new IllegalStateException("MultiSelectAdapter.setItemChecked called without action-mode defined.");

        selectAction.setItemChecked(adapterPosition,checked);
    }

    /**
     * Return an array with the row-ids of the selected items in the recyclerview.
     */
    public long[] getCheckedItemIds() {
        if (selectAction == null) return new long[]{};
        else {
            int nItems = selectAction.checkedItems.size();
            long[] result = new long[nItems];
            for (int i = 0; i < nItems; i++) {
                result[i] = getItemId(selectAction.checkedItems.keyAt(i));
            }
            return result;
        }
    }
}
