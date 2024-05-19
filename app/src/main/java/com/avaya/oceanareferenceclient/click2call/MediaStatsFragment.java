package com.avaya.oceanareferenceclient.click2call;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.avaya.clientservices.call.CallType;
import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.interactions.AbstractInteractionActivity;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Services.Statistics.AudioDetails;
import com.avaya.ocs.Services.Statistics.CallDetails;
import com.avaya.ocs.Services.Statistics.Callbacks.AudioDetailsCallback;
import com.avaya.ocs.Services.Statistics.Callbacks.VideoDetailsCallback;
import com.avaya.ocs.Services.Statistics.VideoDetails;
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction;

import java.util.ArrayList;
import java.util.List;

// Instances of this class are fragments representing a single
// object in our collection.
public class MediaStatsFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    ViewPager viewPager;
    private static final String TAG = MediaStatsFragment.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);
    private Handler mTimerHandler;
    private RecyclerView mRecyclerView;
    private String fps;
    private String kbps;
    private String ms;
    private String packets;
    private String send;
    private String receive;
    private String tls;
    private String udp;
    private String sip_ua;
    private String https_ua;
    private int firstListItem = -1;
    private View firstItemView;
    private int topOffset;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        AbstractInteractionActivity abstractInteractionActivity = (AbstractInteractionActivity) MediaStatsFragment.this.getActivity();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(abstractInteractionActivity);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        // create a handler for the call timer
        mTimerHandler = new Handler();
        initMessages();
        startStatsTimer();
    }

    private void initMessages() {
        fps = getResources().getString(R.string.fps);
        kbps = getResources().getString(R.string.kbps);
        ms = getResources().getString(R.string.ms);
        packets = getResources().getString(R.string.packets);
        send = getResources().getString(R.string.send);
        receive = getResources().getString(R.string.receive);
        tls = getResources().getString(R.string.tls);
        udp = getResources().getString(R.string.udp);
        sip_ua = getResources().getString(R.string.sip_ua);
        https_ua = getResources().getString(R.string.http_ua);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopStatsTimer();
    }

    protected void startStatsTimer() {
        mLogger.d("starting stats timer");
        try {

            mStatisticsUpdateCheck.run();
        } catch (Exception e) {
            mLogger.e("Exception in startStatsTimer", e);
        }
    }

    private Runnable mStatisticsUpdateCheck = new Runnable() {
        @Override
        public void run() {

            initStatistics();
            mTimerHandler.postDelayed(mStatisticsUpdateCheck, Constants.STATS_UPDATE_INTERVAL);
        }
    };

    protected void stopStatsTimer() {
        mLogger.d("stopping stats timer");
        try {
            mTimerHandler.removeCallbacks(mStatisticsUpdateCheck);
        } catch (Exception e) {
            mLogger.e("Exception in stopStatsTimer", e);
        }
    }

    private void initStatistics() {

        AbstractInteractionActivity currentActivity = (AbstractInteractionActivity) MediaStatsFragment.this.getActivity();

        if (currentActivity == null) {
            mLogger.d("Interaction:Activity is no more valid");
            return;
        }
        AbstractInteraction interaction = currentActivity.getInteraction();
        if (interaction == null) {
            mLogger.d("Interaction:Audio/Video interaction is no more valid");
            return;
        }
        Bundle args = getArguments();
        int positionInTab = args.getInt(ARG_OBJECT);
        if (positionInTab == 0) {
            interaction.readAudioDetails(new AudioDetailsCallback() {
                @Override
                public void readComplete(AudioDetails audioDetails) {
                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentScrollPosition();
                            renderAudioDetails(currentActivity, mRecyclerView, audioDetails, interaction);
                            setCurrentScrollPosition();
                        }
                    });

                }
            });
        } else if (positionInTab == 1) {

            interaction.readVideoDetails(new VideoDetailsCallback() {

                @Override
                public void readComplete(VideoDetails videoDetails) {
                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentScrollPosition();
                            renderVideoDetails(currentActivity, mRecyclerView, videoDetails);
                            setCurrentScrollPosition();
                        }
                    });
                }

            });
        }

    }

    /**
     * Save the scroll position before refreshing the content
     */
    private void getCurrentScrollPosition() {
        if (mRecyclerView != null) {
            LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            firstListItem = manager.findFirstVisibleItemPosition();
            firstItemView = manager.findViewByPosition(firstListItem);
            topOffset =  firstItemView!=null? firstItemView.getTop(): 0;
        }
    }
    /**
     * Scroll to the saved position after refreshing the content
     */
    private void setCurrentScrollPosition() {
        if (mRecyclerView != null && firstListItem >= 0) {
            LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            manager.scrollToPositionWithOffset(firstListItem, (int) topOffset);
        }
    }


    private void renderAudioDetails(FragmentActivity currentActivity, RecyclerView recyclerView, AudioDetails audioDetails, AbstractInteraction interaction) {


        String[] audioStatsArr = currentActivity.getResources().getStringArray(R.array.audio_call_stats_array);
        List<StatsItem> statsArr = new ArrayList<StatsItem>(audioStatsArr.length);


        for (int i = 0; i < audioStatsArr.length; i++) {

            StatsItem statsItem;
            switch (i) {
                case 0:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCodec()); // Codec
                    break;
                case 1:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getMediaEncryptionType().name()); // Encryption
                    break;
                case 2:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getPacketizationIntervalMillis() + " " + ms); // Packetization
                    break;
                case 3:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getRoundTripTimeMillis() + " " + ms); // Round trip time
                    break;
                case 4:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getPacketsTransmitted() + " / " + audioDetails.getPacketsReceived()); // Packets transmitted received
                    break;
                case 5:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getBytesTransmitted() + " / " + audioDetails.getBytesReceived()); //
                    break;
                case 6:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getFractionLostTransmitted() + " % / " + audioDetails.getFractionLostReceived() + " %");
                    break;
                case 7:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getAverageJitterTransmittedMillis() + " " + ms + " / " + audioDetails.getAverageJitterReceivedMillis() + " " + ms);
                    break;
                case 8:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCurrentBufferSizeMillis() + " " + ms + " / " + audioDetails.getPreferredBufferSizeMillis() + " " + ms);
                    break;
                case 9:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCurrentPacketLossRate() + " %");
                    break;
                case 10:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCurrentDiscardRate() + " %");
                    break;
                case 11:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCurrentExpandRate() + " %");
                    break;
                case 12:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCurrentPreemptiveRate() + " %");
                    break;
                case 13:
                    statsItem = new StatsItem(audioStatsArr[i], audioDetails.getCurrentAccelerationRate() + " %");
                    break;
                case 14:
                    CallType type = interaction.getCallType();
                    statsItem = new StatsItem(audioStatsArr[i], interaction.getCallType() == CallType.DEFAULT_REGISTERED_CALLTYPE ? sip_ua : https_ua + ""); //ToDo
                    break;
                case 15:
                    String tunneledText = ((CallDetails) audioDetails).isMediaTunnelled() ? tls : udp;
                    statsItem = new StatsItem(audioStatsArr[i], tunneledText);
                    break;
                default:
                    statsItem = null;

            }
            statsArr.add(statsItem);
        }
        StatsAdapter statsAdapter = new StatsAdapter(R.layout.item_stats, statsArr);
        recyclerView.setAdapter(statsAdapter);

        Log.i("AudioDetails", "getAverageJitterReceivedMillis:" + audioDetails.getAverageJitterReceivedMillis());
        Log.i("AudioDetails", "getAverageJitterTransmittedMillis:" + audioDetails.getAverageJitterTransmittedMillis());
        Log.i("AudioDetails", "getCodec:" + audioDetails.getCodec());
        Log.i("AudioDetails", "getLocalIPAddress:" + audioDetails.getLocalIPAddress());
        Log.i("AudioDetails", "getRemoteIPAddress:" + audioDetails.getRemoteIPAddress());
        Log.i("AudioDetails", "getBytesReceived:" + audioDetails.getBytesReceived());
        Log.i("AudioDetails", "getBytesTransmitted:" + audioDetails.getBytesTransmitted());
        Log.i("AudioDetails", "getCurrentAccelerationRate:" + audioDetails.getCurrentAccelerationRate());
        Log.i("AudioDetails", "getCurrentBufferSizeMillis:" + audioDetails.getCurrentBufferSizeMillis());
        Log.i("AudioDetails", "getCurrentDiscardRate:" + audioDetails.getCurrentDiscardRate());
        Log.i("AudioDetails", "getCurrentExpandRate:" + audioDetails.getCurrentExpandRate());
        Log.i("AudioDetails", "getCurrentPacketLossRate:" + audioDetails.getCurrentPacketLossRate());
        Log.i("AudioDetails", "getCurrentPreemptiveRate:" + audioDetails.getCurrentPreemptiveRate());
        Log.i("AudioDetails", "getDTMFPayloadType:" + audioDetails.getDTMFPayloadType());
        Log.i("AudioDetails", "getFractionLostReceived:" + audioDetails.getFractionLostReceived());
        Log.i("AudioDetails", "getFractionLostTransmitted:" + audioDetails.getFractionLostTransmitted());
        Log.i("AudioDetails", "getPacketizationIntervalMillis:" + audioDetails.getPacketizationIntervalMillis());
        Log.i("AudioDetails", "getPacketsReceived:" + audioDetails.getPacketsReceived());
        Log.i("AudioDetails", "getPacketsTransmitted:" + audioDetails.getPacketsTransmitted());
        Log.i("AudioDetails", "getPreferredBufferSizeMillis:" + audioDetails.getPreferredBufferSizeMillis());
        Log.i("AudioDetails", "getLocalPort:" + audioDetails.getLocalPort());
        Log.i("AudioDetails", "getRemotePort:" + audioDetails.getRemotePort());
        Log.i("AudioDetails", "getMediaEncryptionType:" + audioDetails.getMediaEncryptionType());
        Log.i("AudioDetails", "getRoundTripTimeMillis:" + audioDetails.getRoundTripTimeMillis());
        //RtpTransport
        //CallType

    }

    private void renderVideoDetails(FragmentActivity currentActivity, RecyclerView recyclerView, VideoDetails videoDetails) {

        String[] videoStatsArray = currentActivity.getResources().getStringArray(R.array.video_call_stats_array);
        List<StatsItem> statsArr = new ArrayList<StatsItem>(videoStatsArray.length);


        StatsItem statsItemHeaderSend = new StatsItem("" + receive, "", StatsItem.TYPE_HEADER); // Send header
        statsArr.add(statsItemHeaderSend);
        for (int i = 0; i < videoStatsArray.length; i++) {

            StatsItem statsItem;
            switch (i) {
                case 0:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getReceiveStatistics().getWidth() + " x " + videoDetails.getReceiveStatistics().getHeight()); // Resolution
                    break;
                case 1:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getReceiveStatistics().getActualFrameRate() + " " + fps); // Actual Frame rate
                    break;
                case 2:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getCodec()); // Codec
                    break;
                case 3:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getReceiveStatistics().getActualBitRate() + " " + kbps); // Actual bit rate
                    break;
                case 4:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getReceiveStatistics().getTargetBitRate() + " " + kbps); // Target bit rate
                    break;
                case 5:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getRoundTripTimeMillis() + " " + ms); // Round trip time millis
                    break;
                case 6:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getReceiveStatistics().getJitterBufferSizeMillis() + " " + ms); // jitter buffer size millis
                    break;
                case 7:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getReceiveStatistics().getPacketLossTotal() + " " + packets + ", " + videoDetails.getReceiveStatistics().getPacketLossFraction()); // Packet loss
                    break;
                case 8:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getMediaEncryptionType().name() + ""); // Encryption
                    break;
                default:
                    statsItem = null;

            }
            statsArr.add(statsItem);
        }

        StatsItem statsItemHeaderReceived = new StatsItem("" + send, "", StatsItem.TYPE_HEADER); // Send header
        statsArr.add(statsItemHeaderReceived);

        for (int i = 0; i < videoStatsArray.length; i++) {

            StatsItem statsItem;
            switch (i) {
                case 0:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getTransmitStatistics().getWidth() + " x " + videoDetails.getTransmitStatistics().getHeight()); // Resolution
                    break;
                case 1:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getTransmitStatistics().getActualFrameRate() + " " + fps); // Actual Frame rate
                    break;
                case 2:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getCodec()); // Codec
                    break;
                case 3:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getTransmitStatistics().getActualBitRate() + " " + kbps); // Actual bit rate
                    break;
                case 4:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getTransmitStatistics().getTargetBitRate() + " " + kbps); // Target bit rate
                    break;
                case 5:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getRoundTripTimeMillis() + " " + ms); // Round trip time millis
                    break;
                case 6:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getTransmitStatistics().getJitterBufferSizeMillis() + " " + ms); // jitter buffer size millis
                    break;
                case 7:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getTransmitStatistics().getPacketLossTotal() + " " + packets + ", " + videoDetails.getTransmitStatistics().getPacketLossFraction()); // Packet loss
                    break;
                case 8:
                    statsItem = new StatsItem(videoStatsArray[i], videoDetails.getMediaEncryptionType().name() + ""); // Encryption
                    break;
                default:
                    statsItem = null;

            }
            statsArr.add(statsItem);
        }
        StatsAdapter statsAdapter = new StatsAdapter(R.layout.item_stats, statsArr);
        recyclerView.setAdapter(statsAdapter);

    }

}

class StatsItem {
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_HEADER = 2;

    private String statsKey;
    private String statsValue;
    private int itemType;

    public StatsItem(String statsKey, String statsValue) {
        this.statsKey = statsKey;
        this.statsValue = statsValue;
        this.itemType = TYPE_NORMAL;
    }

    public StatsItem(String statsKey, String statsValue, int itemType) {
        this.statsKey = statsKey;
        this.statsValue = statsValue;
        this.itemType = itemType;
    }

    public int getStatsType() {
        return itemType;
    }

    public String getStatsKey() {
        return statsKey;
    }

    public String getStatsValue() {
        return statsValue;
    }

}

class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {


    private final int layoutId;
    private List<StatsItem> statsItems;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvStatsKey;
        public TextView tvStatsValue;
        public TextView tvStatsHeader;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            tvStatsKey = (TextView) v.findViewById(R.id.tvStatsKey);
            tvStatsValue = (TextView) v.findViewById(R.id.tvStatsValue);
            tvStatsHeader = (TextView) v.findViewById(R.id.tvStatsHeader);
        }
    }

    public void add(int position, StatsItem item) {
        statsItems.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        statsItems.remove(position);
        notifyItemRemoved(position);
    }

    public StatsAdapter(int layoutId, List<StatsItem> statsItems) {
        this.layoutId = layoutId;
        this.statsItems = statsItems;
    }

    @Override
    public StatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(layoutId, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final StatsItem statsItem = statsItems.get(position);

        //Show header differently
        if (statsItem.getStatsType() == StatsItem.TYPE_HEADER) {
            holder.tvStatsHeader.setVisibility(View.VISIBLE);
            holder.tvStatsHeader.setText(statsItem.getStatsKey());
            holder.tvStatsKey.setVisibility(View.GONE);
            holder.tvStatsValue.setVisibility(View.GONE);
        } else {
            holder.tvStatsHeader.setVisibility(View.GONE);
            holder.tvStatsKey.setVisibility(View.VISIBLE);
            holder.tvStatsValue.setVisibility(View.VISIBLE);
            holder.tvStatsKey.setText(statsItem.getStatsKey());
            holder.tvStatsValue.setText(statsItem.getStatsValue());
        }
    }

    @Override
    public int getItemCount() {
        return statsItems.size();
    }


}
