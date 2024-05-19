package com.avaya.oceanareferenceclient.interactions;

import com.avaya.callprovider.enums.CallType;
import com.avaya.ocs.Services.Work.Enums.AudioDeviceType;
import com.avaya.ocs.Services.Work.Interactions.AudioDeviceSwitchHelper;

import java.util.Arrays;
import java.util.List;

import static com.avaya.ocs.Services.Work.Enums.AudioDeviceType.BLUETOOTH_HEADSET;
import static com.avaya.ocs.Services.Work.Enums.AudioDeviceType.HANDSET;
import static com.avaya.ocs.Services.Work.Enums.AudioDeviceType.SPEAKER;
import static com.avaya.ocs.Services.Work.Enums.AudioDeviceType.WIRED_HEADSET;

public class MyCustomAudioDeviceSwitchHelper implements AudioDeviceSwitchHelper {

    private static List<AudioDeviceType> audioCallDevicePriorityList =
            Arrays.asList(WIRED_HEADSET, HANDSET, BLUETOOTH_HEADSET, SPEAKER);
    private static List<AudioDeviceType> videoCallDevicePriorityList =
            Arrays.asList(SPEAKER, WIRED_HEADSET, BLUETOOTH_HEADSET, HANDSET);

    private List<AudioDeviceType> callDevicePriorityList;

    public MyCustomAudioDeviceSwitchHelper(CallType callType) {
        if(callType == CallType.AUDIO) {
            callDevicePriorityList = audioCallDevicePriorityList;
        } else  {
            callDevicePriorityList = videoCallDevicePriorityList;
        }
    }

    @Override
    public AudioDeviceType getPrioritizedDevice(List<AudioDeviceType> list) {
        for (AudioDeviceType device: callDevicePriorityList) {
            if(list.contains(device)) {
                return device;
            }
        }
        return null;
    }
}
