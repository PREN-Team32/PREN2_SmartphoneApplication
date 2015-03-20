package ch.pren.multimedia;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

import ch.pren.androidapp.R;

/**
 * Created by Thomas on 19.03.2015.
 */
public class SoundHandler {

    private SoundPool soundPool;
    private int soundID;
    private boolean plays = false;
    private boolean loaded = false;
    private float actVolume, maxVolume, volume;
    private AudioManager audioManager;
    private int counter;
    private int sID = R.raw.airhorn;
    private Context context;


    /**
     * Context der Main_Activity mitgeben.
     * @param context
     */
    public SoundHandler(Context context){

        this.context = context;


        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = maxVolume;

        //Hardware buttons setting to adjust the media sound
        //this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // TODO check if code above is needed

        // the counter will help us recognize the stream id of the sound played  now
        counter = 0;

        // Load the sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(context, sID, 1);


    }

    public void play(){
        // Is the sound loaded does it already play?
        if (loaded && !plays) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
            counter = counter++;
            Toast.makeText(context, "Played sound", Toast.LENGTH_SHORT).show();
            plays = true;
        }
    }

    public void pause(){
        // Is the sound loaded already?
        if (plays) {
            soundPool.pause(soundID);
            soundID = soundPool.load(context, sID, counter);
            Toast.makeText(context, "Pause sound", Toast.LENGTH_SHORT).show();
            plays = false;
        }
    }

    public void stop(){

        // Is the sound loaded already?
        if (plays) {
            soundPool.stop(soundID);
            soundID = soundPool.load(context, sID, counter);
            Toast.makeText(context, "Stop sound", Toast.LENGTH_SHORT).show();
            plays = false;
        }
    }
}
