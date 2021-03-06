package com.devoxx.wear.fragment;

import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devoxx.R;
import com.devoxx.common.utils.Constants;
import com.devoxx.event.AddFavoriteEvent;
import com.devoxx.event.FavoriteEvent;
import com.devoxx.event.GetTalkEvent;
import com.devoxx.event.RemoveFavoriteEvent;
import com.devoxx.event.TalkEvent;
import com.devoxx.model.TalkFullApiModel;

import java.text.SimpleDateFormat;
import java.util.Date;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 24/08/15.
 */
public class TalkFragment extends BaseFragment {

    private final static String TAG = TalkFragment.class.getCanonicalName();


    private View mMainView;

    private TalkFullApiModel mTalk;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        final String pageTitle = (getArguments() != null ? getArguments().getString(Constants.DATAMAP_TITLE) : "");


        mMainView = inflater.inflate(R.layout.talk_fragment, container, false);


        WatchViewStub stub = (WatchViewStub) mMainView.findViewById(R.id.watch_talk_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // Set the title if any
                ((TextView) mMainView.findViewById(R.id.title)).setText(pageTitle);

                // add event listener
                mMainView.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTalk == null) {
                            return;
                        }

                        String confirmationMessage;
                        if (mTalk.getFavorite()) {
                            // remove from my favorites
                            confirmationMessage = getString(R.string.remove_favorites);
                            EventBus.getDefault().postLocal(new RemoveFavoriteEvent(mTalk.getId()));
                        } else {
                            // add to my favorites
                            confirmationMessage = getString(R.string.add_favorites);
                            EventBus.getDefault().postLocal(new AddFavoriteEvent(mTalk.getId()));
                        }

                        startConfirmationActivity(ConfirmationActivity.SUCCESS_ANIMATION, confirmationMessage);
                    }
                });
            }
        });

        return mMainView;
    }



    @Override
    public void onResume() {
        super.onResume();

        if (mTalk == null) {
            EventBus.getDefault().postLocal(new GetTalkEvent());

        } else {
            displayTalk();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(final TalkEvent talkEvent) {

        if (talkEvent == null) {
            return;
        }

        mTalk = talkEvent.getTalk();

        displayTalk();


    }


    public void onEvent(final FavoriteEvent favoriteEvent) {
        if (favoriteEvent == null) {
            return;
        }

        boolean favorite = favoriteEvent.getFavorite();

        mTalk.setFavorite(favorite);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTalk.getFavorite()) {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_on);
                } else {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_off);
                }
            }
        });
    }


    private void displayTalk() {

        if (mTalk == null) {
            return;
        }

        if (getActivity() == null) {
            return;
        }

        if (mMainView == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTalk == null) {
                    Log.d(TAG, "mTalk is null");
                }

                if (mTalk.getTitle() == null) {
                    Log.d(TAG, "mTalk.title is null");
                }


                ((TextView) mMainView.findViewById(R.id.title)).setText(mTalk.getTitle());

                ((TextView) mMainView.findViewById(R.id.roomName)).setText(mTalk.getRoomName());

                if (mTalk.getFavorite()) {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_on);
                } else {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_off);
                }

                String timeFrom;
                String timeTo;
                String dayOfWeek;

                ((TextView) mMainView.findViewById(R.id.talkTime)).setText("");

                if (mTalk.getFromTimeMillis() != null) {
                    timeFrom = new SimpleDateFormat("HH:mm").format(new Date(mTalk.getFromTimeMillis()));
                    dayOfWeek = new SimpleDateFormat("EEEE").format(new Date(mTalk.getFromTimeMillis()));

                } else {
                    return;
                }

                if (mTalk.getToTimeMillis() != null) {
                    timeTo = new SimpleDateFormat("HH:mm").format(new Date(mTalk.getToTimeMillis()));
                } else {
                    return;
                }

                ((TextView) mMainView.findViewById(R.id.dayOfWeek)).setText(dayOfWeek);

                ((TextView) mMainView.findViewById(R.id.talkTime)).setText(timeFrom + " - " + timeTo);
            }
        });

    }


}
