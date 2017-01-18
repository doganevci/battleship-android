package com.dogan.amiral;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;



import static com.dogan.amiral.game.gameProcess.LAST_AIM_POSITION;
import static com.dogan.amiral.game.gameProcess.THE_ENEMY_BOARD_HITS;



public class ImageEnemyAdapter extends BaseAdapter {
    private Context mContext;
    View lastview;

    public ImageEnemyAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 225;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);

        } else {
            imageView = (ImageView) convertView;
        }


        if(THE_ENEMY_BOARD_HITS.get(position)==1)
        {
            imageView.setBackgroundColor(Color.BLACK);
        }
        else if(THE_ENEMY_BOARD_HITS.get(position)==2)  // hit but not damage
        {
            imageView.setBackgroundColor(Color.LTGRAY);
        }
        else if(THE_ENEMY_BOARD_HITS.get(position)==3)  // probe area
        {
            imageView.setBackgroundColor(Color.GREEN);
        }
        else if(LAST_AIM_POSITION!=-1 && position==LAST_AIM_POSITION)
        {
            imageView.setBackgroundColor(Color.RED);
        }
        else
        {
            imageView.setBackgroundColor(Color.BLUE);
        }


        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show();
                
                LAST_AIM_POSITION=position;

                if(lastview!=null)
                lastview.setBackgroundColor(Color.BLUE);
                view.setBackgroundColor(Color.RED);

                lastview=view;

                return false;
            }
        });


        return imageView;
    }



}