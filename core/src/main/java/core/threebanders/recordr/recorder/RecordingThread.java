package core.threebanders.recordr.recorder;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import core.threebanders.recordr.Core;
import core.threebanders.recordr.R;

abstract class RecordingThread {
    static final int SAMPLE_RATE = 44100;
    protected final Recorder recorder;
    protected final Context context;
    final int channels;
    final int bufferSize;
    final AudioRecord audioRecord;

    RecordingThread(Context context, String mode, Recorder recorder) throws RecordingException {
        this.context = context;
        channels = (mode.equals(Recorder.MONO) ? 1 : 2);
        this.recorder = recorder;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, channels == 1 ?
                        AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = createAudioRecord();
        audioRecord.startRecording();
    }

    //e statică ca să poată fi apelată din CopyPcmToWav
    static void notifyOnError(Context context) {
        RecorderService service = RecorderService.getService();
        if (service != null) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null)
                nm.notify(RecorderService.NOTIFICATION_ID,
                        service.buildNotification(RecorderService.RECORD_ERROR, context.getString(R.string.recorder_fail)));
        }
    }

    @SuppressLint("MissingPermission")
    private AudioRecord createAudioRecord() throws RecordingException {
        AudioRecord audioRecord;
        int source = Integer.parseInt(Core.getInstance().getCache().source());

        try {
            audioRecord = new AudioRecord(source, SAMPLE_RATE,
                    channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);

        } catch (Exception e) { //La VOICE_CALL dă IllegalArgumentException. Aplicația nu se oprește, rămîne
            //hanging, nu înregistrează nimic.
            Log.d("TAG","VOICE CALL EXCEPTION" + e.getMessage());
            throw new RecordingException(e.getMessage());

        }

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.setSource(audioRecord.getAudioSource());
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            throw new RecordingException(context.getString(R.string.unable_to_init_audio_record));

        return audioRecord;
    }

    void disposeAudioRecord() {
        audioRecord.stop();
        audioRecord.release();
    }
}
