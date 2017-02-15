package fr.epicture.epicture.flickr.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.epicture.epicture.R;
import fr.epicture.epicture.flickr.interfaces.ImageListAdapterInterface;
import fr.epicture.epicture.flickr.recyclers.ImageListRecyclerHeaderViewHolder;
import fr.epicture.epicture.flickr.recyclers.ImageListRecyclerItemViewHolder;
import fr.epicture.epicture.flickr.utils.ImageElement;

/**
 * Created by Stephane on 15/02/2017.
 */

public class ImageListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    private List<ImageElement> imageElementList;

    private Activity activity;
    private boolean header;
    private ImageListAdapterInterface listener;

    public ImageListRecyclerAdapter(Activity activity, boolean header, ImageListAdapterInterface listener) {
        this.imageElementList = new ArrayList<>();

        this.activity = activity;
        this.header = header;
        this.listener = listener;
    }

    public void clear() {
        imageElementList.clear();
    }

    public void addList(List<ImageElement> imageElementList) {
        this.imageElementList.addAll(imageElementList);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagelist_item, parent, false);
            return (ImageListRecyclerItemViewHolder.newInstance(activity, view, listener));
        }
        else if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagelist_header, parent, false);

            TypedValue tv = new TypedValue();
            if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                int toolbarSize = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
                int top = 0;

                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams)view.getLayoutParams();
                lp.setMargins(0, toolbarSize + top, 0, 0);
                view.invalidate();
            }

            return (new ImageListRecyclerHeaderViewHolder(view));
        }
        return (null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!isHeader(position)) {
            position -= ((header) ? 1 : 0);
            ImageElement item = getItem(position);

            ((ImageListRecyclerItemViewHolder)holder).refreshView(item);
            ((ImageListRecyclerItemViewHolder)holder).refreshImage(item);
        }
    }

    private ImageElement getItem(int index) {
        return imageElementList.get(index);
    }

    private int getBasicItemCount() {
        return (imageElementList.size());
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public int getItemCount() {
        return (getBasicItemCount() + (header?1:0));
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isHeader(int position) {
        return (position == 0 && header);
    }
}