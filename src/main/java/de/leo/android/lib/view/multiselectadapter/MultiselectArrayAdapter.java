package de.leo.android.lib.view.multiselectadapter;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;


/**
 * Multiselect adapter for RecyclerViews with cursors as datasource.
 *
 * Created by Matthias Leonhardt on 03.10.16.
 *
 * @param <DataObject>              Type of the data-objects that are to be displayed through this
 *                                  adapter.
 * @param <BindingObjectFactory>    Factory to create the view for an item to be displayed
 */
public class MultiselectArrayAdapter<DataObject, BindingObjectFactory extends MultiselectAdapter.DataBindingFactory> extends MultiselectAdapter<BindingObjectFactory> {

    /**
     * Extend the {@link MultiselectAdapter.ActionDataBinding} interface with
     * a method to assign data to the detail-view.
     */
    public interface ArrayActionBinding<DataObject> extends ActionDataBinding<DataObject> {
        void setData(DataObject data);
//        public abstract long getItemId();
    }


    public static class MultiselectArrayViewHolder<BindingObject> extends MultiselectViewHolder {

/*
//        public static MultiselectArrayViewHolder createInstance(ActionDataBinding binding, MultiselectAction selectAction, Type BindingObject1) {
        public static MultiselectArrayViewHolder createInstance(ArrayActionBinding binding, MultiselectAction selectAction) {

            MultiselectArrayViewHolder vh = new MultiselectArrayViewHolder(binding.getRoot());
            vh.binding = binding;
            vh.selectAction = selectAction;

            return vh;
        }

        private MultiselectArrayViewHolder(View itemView) { super(itemView); }
*/
        public MultiselectArrayViewHolder(ArrayActionBinding binding, MultiselectAction selectAction) {
            super(binding.getRoot());

            this.binding = binding;
            this.selectAction = selectAction;
        }


        public void setData(BindingObject data) {
            ((ArrayActionBinding)binding).setData(data);
            binding.getRoot().setActivated(selectAction.isActive() && selectAction.getCheckedItems().indexOfKey(getAdapterPosition()) >= 0);
        }
    }


    /**
     * Contains the data that is displayed by this adapter
     */
    private List<DataObject> data;


    public MultiselectArrayAdapter(AppCompatActivity activity, BindingObjectFactory factoryObject, List<DataObject> data, ActionMode.Callback callback) {
        super(activity, factoryObject, callback);

setHasStableIds(false);

        this.data = data;
    }

    @Override
    public MultiselectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ActionDataBinding binding = factoryObject.inflate(LayoutInflater.from(parent.getContext()), parent, true);
        return new MultiselectArrayViewHolder((ArrayActionBinding)binding, selectAction);
    }

    @Override
    public void onBindViewHolder(MultiselectViewHolder holder, int position) {
        ((MultiselectArrayViewHolder)holder).setData(data.get(position));
    }

    @Override
    public int getItemCount() { return (data == null) ? 0 : data.size(); }

    public void swapNewData(List<DataObject> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
/*
        return RecyclerView.NO_ID;
        if (data != null) {
            return data.get(position).getItemId();
        } else return RecyclerView.NO_ID;
*/
    }
}
