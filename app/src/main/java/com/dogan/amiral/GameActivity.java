package com.dogan.amiral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dogan.amiral.MessagerModule.OneComment;
import com.dogan.amiral.Network.ReceiverClientThread;
import com.dogan.amiral.Network.ReceiverServerThread;
import com.dogan.amiral.game.enums.shipType;
import com.dogan.amiral.game.gameProcess;
import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;
import com.dogan.amiral.models.MovementModel;
import com.dogan.amiral.models.messageModel;


import static com.dogan.amiral.game.gameProcess.IS_MY_TURN;
import static com.dogan.amiral.game.gameProcess.LAST_AIM_POSITION;
import static com.dogan.amiral.game.gameProcess.THE_ENEMY_BOARD_HITS;


public class GameActivity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static ViewPager mViewPager;


    public static String PORT=GENERALPROPERTIES.PORT;
    public static ReceiverClientThread chatClientThread = null;
    public static ReceiverServerThread chatServerThread=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        GridView gridView,gridViewEnemy;
        ImageAdapter theAdapter;
        ImageEnemyAdapter theEnemyAdapter;

        LinearLayout layoutShipper,layoutStartGame,layoutBomber;


        Spinner spinnerShip;
        Button btnGenerate,btnStartGame,btnFire;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_game, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            spinnerShip = (Spinner) rootView.findViewById(R.id.spinnerShip);
            btnGenerate = (Button) rootView.findViewById(R.id.btnGenerate);
            btnStartGame= (Button) rootView.findViewById(R.id.btnStartGame);
            btnFire= (Button) rootView.findViewById(R.id.btnFire);


            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mGameNotifReceiver,
                    new IntentFilter("mGameNotifReceiver"));

            layoutShipper = (LinearLayout) rootView.findViewById(R.id.layoutShipper);
            layoutStartGame = (LinearLayout) rootView.findViewById(R.id.layoutStartGame);
            layoutBomber = (LinearLayout) rootView.findViewById(R.id.layoutBomber);


            if(getArguments().getInt(ARG_SECTION_NUMBER)==2)   //ENEMY Screen
            {
                textView.setText("My Board");
                layoutShipper.setVisibility(View.VISIBLE);
                layoutStartGame.setVisibility(View.VISIBLE);

                btnStartGame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        boolean canStartGame=true;

                        for (int remain:gameProcess.shipToChooseRemain)
                        {
                            if(remain>0)
                            {
                                canStartGame=false;
                                break;
                            }
                        }


                        if(canStartGame)
                        {
                            layoutShipper.setVisibility(View.GONE);
                            layoutStartGame.setVisibility(View.GONE);


                            if( IS_MY_TURN)
                            {
                                Toast.makeText(getActivity(), "Game has begun! First move is yours.", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(getActivity(), "Game has begun!Enemy will do the first move!", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Cant start game, unless you dont put all ships!", Toast.LENGTH_SHORT).show();
                        }


                    }
                });


                btnGenerate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if(gameProcess.shipToChooseRemain.get(spinnerShip.getSelectedItemPosition())>0)
                        {
                            gameProcess.ADD_SHIP(spinnerShip.getSelectedItemPosition());

                            theAdapter.notifyDataSetChanged();

                            int cnt=gameProcess.shipToChooseRemain.get(spinnerShip.getSelectedItemPosition());
                            gameProcess.shipToChooseRemain.set(spinnerShip.getSelectedItemPosition(),cnt-1);
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Can't choose this type of ship anymore!", Toast.LENGTH_SHORT).show();
                        }
                        

                    }
                });




                gameProcess.PrepareGame();


                gridView=(GridView)rootView.findViewById(R.id.gridView);

                theAdapter=new ImageAdapter(getActivity());
                gridView.setAdapter(theAdapter);
                gridView.setVisibility(View.VISIBLE);

            }else if(getArguments().getInt(ARG_SECTION_NUMBER)==3)
            {
                textView.setText("Enemy's Board");

                layoutBomber.setVisibility(View.VISIBLE);


                btnFire.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                if(IS_MY_TURN)
                {
                    sendFire(LAST_AIM_POSITION,1);
                }
                        else
                {
                    Toast.makeText(getActivity(), "Wait for the Enemy. its his turn now!", Toast.LENGTH_SHORT).show();
                }



                    }
                });






                gameProcess.PrepareGame();


                gridViewEnemy=(GridView)rootView.findViewById(R.id.gridViewEnemy);
                theEnemyAdapter=new ImageEnemyAdapter(getActivity());
                gridViewEnemy.setAdapter(theEnemyAdapter);
                gridViewEnemy.setVisibility(View.VISIBLE);

            }


            return rootView;
        }





        private BroadcastReceiver mGameNotifReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                IS_MY_TURN=true;


                int type = intent.getIntExtra("type",0);
                boolean isFired = intent.getBooleanExtra("isfired",false);

                if(type==1)
                {
                    IS_MY_TURN=false;

                   if(isFired)
                   {
                       Toast.makeText(getActivity(), "Successfull attemp, you got fired a ship!", Toast.LENGTH_SHORT).show();
                   }
                    else
                   {
                       Toast.makeText(getActivity(), "You have failed about your fire attempt!", Toast.LENGTH_SHORT).show();
                   }

                    mViewPager.setCurrentItem(2);
                }
                else if(type == 2)
                {


                    if(isFired)
                    {
                        Toast.makeText(getActivity(), "Got Fired !! Alarm!!", Toast.LENGTH_SHORT).show();
                        IS_MY_TURN=true;
                    }
                    else
                    {
                        IS_MY_TURN=true;
                        Toast.makeText(getActivity(), "So close, now its your turn mate!", Toast.LENGTH_SHORT).show();
                    }


                    mViewPager.setCurrentItem(1);
                }
                
                
              refreshMessageList();




            }
        };


        public void refreshMessageList()
        {





            try
            {

                gridView.invalidateViews();
                gridView.setAdapter(theAdapter);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }



            try
            {

                gridViewEnemy.invalidateViews();
                gridViewEnemy.setAdapter(theEnemyAdapter);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


        }




        public void sendFire(int coordinate, int type)
        {



            Log.i("SENDMESSAGEBTN::","clicked");

            MovementModel mm=new MovementModel();
            mm.setApproval(false);
            mm.setCoordinate(coordinate);
            mm.setMyFireHitTheShip(false);
            mm.setType(type);



            GenericSendReceiveModel genNew=new GenericSendReceiveModel();
            genNew.setType(2);
            genNew.setGameMovement(mm);



            if(chatClientThread==null){


                chatServerThread.getConnectedThread().getSenderThread().sendMsg(genNew);

            }
            else
            {
                chatClientThread.getSenderThread().sendMsg(genNew);
            }


            THE_ENEMY_BOARD_HITS.set(coordinate,2);
            AllLists.THE_MESSAGE_LIST.add(genNew.getMessage());


           // refreshMessageList();

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            if(position==0)
            {
                MainFragment fragment = new MainFragment();
                Bundle args = new Bundle();
            //    args.putInt(ARG_SECTION_NUMBER, sectionNumber);
                fragment.setArguments(args);



                //   return PlaceholderFragment.newInstance(position + 1);
               return  fragment;
            }
            else if(position==1)
            {
                return PlaceholderFragment.newInstance(position + 1);
            }
            else if(position==2)
            {
                return PlaceholderFragment.newInstance(position + 1);
            }
            else
            {
                return PlaceholderFragment.newInstance(position + 1);
            }



        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
