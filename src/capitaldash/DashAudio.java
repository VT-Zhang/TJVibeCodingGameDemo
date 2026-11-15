package capitaldash;

import javax.sound.sampled.*;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Original brass-and-drum march plus synthesized effects, mixed off Swing's thread. */
final class DashAudio implements AutoCloseable {
    static final int RATE=22050;
    private static final int[] MELODY={
        60,64,67,67,69,67,64,62, 60,64,67,72,71,69,67,0,
        65,69,72,72,71,69,67,64, 62,67,71,69,67,64,60,0
    };
    private static final int[] BASS={48,48,53,55};
    private final ConcurrentLinkedQueue<DashModel.Cue> pending=new ConcurrentLinkedQueue<>();
    private volatile boolean playing,muted,unavailable,clearRequested;
    private volatile long generation;
    private volatile SourceDataLine output;
    private static final class Voice {
        final DashModel.Cue cue;int frame;
        Voice(DashModel.Cue cue){this.cue=cue;}
    }
    synchronized void start(){
        if(unavailable||output!=null)return;
        if(GraphicsEnvironment.isHeadless()){unavailable=true;return;}
        long session=++generation;
        Thread worker=new Thread(()->stream(session),"capital-dash-audio");worker.setDaemon(true);worker.start();
    }
    void configure(boolean playing,boolean muted){
        if(this.muted!=muted){clearEffects();}
        this.playing=playing;this.muted=muted;
    }
    boolean unavailable(){return unavailable;}
    void play(DashModel.Cue cue){if(!muted&&!unavailable&&pending.size()<48)pending.offer(cue);}
    void clearEffects(){pending.clear();clearRequested=true;}
    private void stream(long session){
        SourceDataLine line=null;
        try{
            AudioFormat format=new AudioFormat(RATE,16,1,true,false);
            line=AudioSystem.getSourceDataLine(format);line.open(format,4096);
            synchronized(this){if(session!=generation){line.close();return;}output=line;}
            line.start();
            byte[] bytes=new byte[1024];long musicFrame=0;
            List<Voice> voices=new ArrayList<>();
            while(session==generation){
                if(clearRequested){voices.clear();clearRequested=false;}
                DashModel.Cue cue;
                while((cue=pending.poll())!=null){if(voices.size()>=12)voices.remove(0);voices.add(new Voice(cue));}
                boolean music=playing&&!muted;
                for(int i=0;i<bytes.length/2;i++){
                    double sample=music?musicSample(musicFrame++/(double)RATE):0;
                    for(Voice voice:voices)sample+=effectSample(voice.cue,voice.frame++/(double)RATE);
                    if(muted)sample=0;
                    short value=(short)(Math.tanh(sample)*26000);
                    bytes[i*2]=(byte)value;bytes[i*2+1]=(byte)(value>>8);
                }
                voices.removeIf(v->v.frame/(double)RATE>=duration(v.cue));
                line.write(bytes,0,bytes.length);
            }
        }catch(LineUnavailableException|IllegalArgumentException|IllegalStateException|SecurityException e){
            if(session==generation)unavailable=true;
        }finally{
            if(line!=null){line.stop();line.flush();line.close();}
            synchronized(this){if(output==line)output=null;}
        }
    }
    /** Eight-second original major-key march; brass melody, alternating bass and snare. */
    static double musicSample(double t){
        double local=t%8, noteTime=local%.25;
        int step=(int)(local/.25), note=MELODY[step];
        double melody=0;
        if(note!=0){
            double phase=2*Math.PI*hz(note)*noteTime;
            melody=(Math.sin(phase)+.3*Math.sin(phase*2)+.14*Math.sin(phase*3))*.11*envelope(noteTime,.235);
        }
        double beat=local%.5;
        double bass=.08*Math.sin(2*Math.PI*hz(BASS[(int)(local/2)])*beat)*envelope(beat,.32);
        double drum=0;
        if(beat<.095){
            if((int)(local/.5)%2==0)drum=.13*Math.sin(2*Math.PI*(85*beat-170*beat*beat))*Math.exp(-beat*35);
            else drum=.065*(Math.sin(beat*17777)+Math.sin(beat*28311))*Math.exp(-beat*45);
        }
        return melody+bass+drum;
    }
    static double duration(DashModel.Cue cue){return switch(cue){
        case BURGER->.7;case WIN,BOSS_DEFEAT->1.25;case LIFE_LOST->.8;
        case CHECKPOINT->.5;case HURT->.26;default->.18;
    };}
    static double effectSample(DashModel.Cue cue,double t){
        double length=duration(cue);if(t<0||t>=length)return 0;
        double phase;
        switch(cue){
            case BURGER->{int[] notes={60,64,67,72,76};double part=length/notes.length;phase=2*Math.PI*hz(notes[Math.min(notes.length-1,(int)(t/part))])*(t%part);return .24*Math.sin(phase)*envelope(t%part,part);}
            case WIN,BOSS_DEFEAT->{int[] notes={60,64,67,72,67,72};double part=length/notes.length;phase=2*Math.PI*hz(notes[Math.min(notes.length-1,(int)(t/part))])*(t%part);return .25*(Math.sin(phase)+.2*Math.sin(phase*2))*envelope(t%part,part);}
            case LIFE_LOST->phase=2*Math.PI*(420*t-190*t*t);
            case JUMP->phase=2*Math.PI*(320*t+1400*t*t);
            case STAR->phase=2*Math.PI*(t<.08?1000*t:1300*t-24);
            case CHECKPOINT->phase=2*Math.PI*hz(t<.16?64:t<.32?67:72)*t;
            case HURT->phase=2*Math.PI*(180*t-200*t*t);
            case BOSS_SHOT->phase=2*Math.PI*(260*t-500*t*t);
            case BOX,STOMP,BOSS_HIT->phase=2*Math.PI*(150*t-240*t*t);
            default->throw new AssertionError(cue);
        }
        return .22*Math.sin(phase)*envelope(t,length);
    }
    private static double hz(int midi){return 440*Math.pow(2,(midi-69)/12.0);}
    private static double envelope(double t,double length){return Math.max(0,Math.min(1,t/.008)*Math.min(1,(length-t)/.025));}
    @Override public synchronized void close(){
        generation++;pending.clear();
        SourceDataLine line=output;if(line!=null){line.stop();line.flush();line.close();output=null;}
    }
}
