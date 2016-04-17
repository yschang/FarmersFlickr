package com.farmers.farmersflickr;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.origamilabs.library.views.StaggeredGridView;

public class StaggeredAdapter extends ArrayAdapter<String> {

    private ImageLoader mLoader;

    public StaggeredAdapter(Context context, int textViewResourceId,
                            String[] objects) {
        super(context, textViewResourceId, objects);
        mLoader = new ImageLoader(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(getContext());
            convertView = layoutInflator.inflate(R.layout.row_staggered,
                    null);
            holder = new ViewHolder();
            holder.imageView = (ScaleImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        mLoader.DisplayImage(getItem(position), holder.imageView);

        // temporary
        final int tempPosition = position;

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("handleMessage", "position: " + tempPosition);
                Log.d("handleMessage", "getItem(): " + getItem(tempPosition));
                Intent intent = new Intent(getContext(), FullImageActivity.class);
                intent.putExtra("IMAGE_URI", getItem(tempPosition));
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ScaleImageView imageView;
    }
}