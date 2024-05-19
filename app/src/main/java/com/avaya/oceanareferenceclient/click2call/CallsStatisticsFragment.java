package com.avaya.oceanareferenceclient.click2call;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.interactions.AbstractInteractionActivity;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction;
import com.google.android.material.tabs.TabLayout;

public class CallsStatisticsFragment extends DialogFragment {
    private static final String TAG = CallsStatisticsFragment.class.getSimpleName();
    // When requested, this adapter returns a MediaStatsFragment,
    // representing an object in the collection.
    CallStatsPagerAdapter callStatsPagerAdapter;
    ViewPager viewPager;
    private Logger mLogger = Logger.getLogger(TAG);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AbstractInteractionActivity abstractInteractionActivity = (AbstractInteractionActivity) CallsStatisticsFragment.this.getActivity();
        int interactionType = abstractInteractionActivity.getInteractionType();
        AbstractInteraction interaction = abstractInteractionActivity.getInteraction();
        callStatsPagerAdapter = new CallStatsPagerAdapter(getChildFragmentManager(), interactionType, interaction,abstractInteractionActivity);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(callStatsPagerAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }


}

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
class CallStatsPagerAdapter extends FragmentPagerAdapter {
    private final int interactionType;
    private final AbstractInteraction interaction;
    private String audioStatisticsText;
    private String videoStatisticsText;

    public CallStatsPagerAdapter(FragmentManager fm, int interactionType, AbstractInteraction interaction, AbstractInteractionActivity abstractInteractionActivity) {
        super(fm);
        this.interactionType = interactionType;
        this.interaction = interaction;
        audioStatisticsText = abstractInteractionActivity.getString(R.string.audio_statistics);
        videoStatisticsText = abstractInteractionActivity.getString(R.string.video_statistics);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new MediaStatsFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(MediaStatsFragment.ARG_OBJECT, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return interactionType == AbstractInteractionActivity.INTERACTION_AUDIO ? 1 : 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 1:
                return videoStatisticsText;
            default:
                return audioStatisticsText;
        }
    }
}

