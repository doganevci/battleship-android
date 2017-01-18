package com.dogan.amiral;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.dogan.amiral.game.enums.shipType;

import static com.dogan.amiral.game.gameProcess.THE_ENEMY_BOARD_HITS;
import static com.dogan.amiral.game.gameProcess.THE_MY_BOARD;
import static com.dogan.amiral.game.gameProcess.THE_MY_BOARD_HITS;


public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
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



        if(THE_MY_BOARD.get(position)== shipType.NONE)
        {
            imageView.setBackgroundColor(Color.BLUE);
        }
        else
        { imageView.setBackgroundColor(Color.GREEN);

        }


        if(THE_MY_BOARD_HITS.get(position)==1)
        {
            imageView.setBackgroundColor(Color.BLACK);
        }
        else if(THE_MY_BOARD_HITS.get(position)==2)  // hit but not damage
        {
            imageView.setBackgroundColor(Color.LTGRAY);
        }


        return imageView;


    }



}