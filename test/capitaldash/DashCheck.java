package capitaldash;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

/** Deterministic gameplay and rendering checks, no display or external libraries required. */
public final class DashCheck {
    private static final double DT=1.0/120;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static DashModel game(){DashModel game=new DashModel();game.start();return game;}
    public static void main(String[] args)throws Exception{
        DashModel g=game();double x=g.x;g.update(DT,1,false);check(g.x>x,"horizontal movement");
        g.jump();double y=g.y;g.update(DT,0,false);check(g.y<y&&g.vy<0,"jump lifts player");
        for(int i=0;i<150;i++)g.update(DT,0,false);
        check(g.grounded&&g.y==DashModel.FLOOR-DashModel.PH,"gravity lands player");
        g.pause();x=g.x;y=g.y;g.jump();g.update(1,1,true);check(g.x==x&&g.y==y,"pause freezes play");g.pause();
        g.x=250;g.update(DT,0,false);check(g.powered&&g.health==5&&g.maxHealth==5,"Big Mac grants extra hearts");
        g=game();g.x=380;g.y=442;g.grounded=false;g.vy=-500;
        for(int i=0;i<20;i++)g.update(DT,0,false);
        check(g.platforms.stream().anyMatch(p->p.box&&p.used),"underside hit opens power box");
        check(g.pickups.stream().filter(p->p.burger).count()==2,"power box spawns burger once");
        g=game();g.x=250;double feet=g.y+g.playerHeight();g.update(DT,0,false);
        check(g.grown&&g.playerWidth()==45&&g.playerHeight()==72,"burger grows collision body by 50 percent");
        check(Math.abs(g.y+g.playerHeight()-feet)<.001,"growth keeps feet planted");
        check(g.drainSounds().contains(DashModel.Cue.BURGER),"growth emits power-up sound");
        g.x=90;double poweredStart=g.y+g.playerHeight(),poweredPeak=poweredStart;
        g.jump();for(int i=0;i<130;i++){g.update(DT,0,false);poweredPeak=Math.min(poweredPeak,g.y+g.playerHeight());}
        check(g.grounded,"large player lands on solid ground");
        g.x=1110;g.y=400;g.vy=100;g.grounded=false;
        for(int i=0;i<40;i++)g.update(DT,0,false);
        check(g.grounded&&Math.abs(g.y+g.playerHeight()-500)<.001,"grown body lands correctly on elevated platform");
        g.x=90;g.y=DashModel.FLOOR-g.playerHeight();g.grounded=true;g.vy=0;
        DashModel normal=game();double normalStart=normal.y+normal.playerHeight(),normalPeak=normalStart;
        normal.jump();for(int i=0;i<130;i++){normal.update(DT,0,false);normalPeak=Math.min(normalPeak,normal.y+normal.playerHeight());}
        check(poweredStart-poweredPeak>(normalStart-normalPeak)*1.6,"powered jump is substantially higher");
        g=game();g.x=380;g.y=442;g.grounded=true;
        g.pickups.add(new DashModel.Pickup(380,445,true));g.update(DT,0,false);
        check(g.powered&&!g.grown,"growth waits when there is no ceiling clearance");
        g.x=200;g.update(DT,0,false);check(g.grown,"growth happens when clear of low ceiling");
        for(DashModel.Platform platform:g.platforms)check(!DashModel.overlap(g.x,g.y,g.playerWidth(),g.playerHeight(),platform.x,platform.y,platform.w,platform.h),"growth cannot embed player in geometry");
        g.x=890;g.y=860;g.update(DT,0,false);check(!g.grown&&!g.powered&&g.playerHeight()==48,"life loss restores normal size");
        g=game();DashModel.Enemy enemy=g.enemies.get(0);
        g.x=enemy.x;g.y=enemy.y-DashModel.PH-1;g.vy=250;g.grounded=false;
        g.update(DT,0,false);check(!enemy.alive&&g.vy<0&&g.coins>=3,"enemy stomp bounces and rewards");
        g=game();enemy=g.enemies.get(0);g.x=enemy.x;g.y=DashModel.FLOOR-DashModel.PH;
        g.update(DT,0,false);check(g.health==2,"side contact damages");g.update(DT,0,false);check(g.health==2,"damage grace period");
        g=game();g.x=890;g.y=860;g.update(DT,0,false);
        check(g.lives==2&&g.x==90&&g.health==3,"pit loses life and respawns");
        g.x=1900;g.update(DT,0,false);check(g.checkpoint==1810,"Mall checkpoint activates");
        g.y=860;g.update(DT,0,false);check(g.x==1810,"pit returns to checkpoint");
        g.y=860;g.update(DT,0,false);check(g.state==DashModel.State.OVER,"last life ends run");
        g=game();g.x=3150;g.update(DT,0,false);check(g.bossActive&&g.checkpoint==3140,"boss activates arena checkpoint");
        g.x=3110;g.update(DT,-1,true);check(g.x>=3110,"arena gate prevents retreat");
        g.bossCooldown=.001;g.update(DT,0,false);check(!g.shots.isEmpty(),"boss launches projectiles");
        g.pause();double shotX=g.shots.get(0).x;g.update(1,0,false);check(g.shots.get(0).x==shotX,"pause freezes boss attacks");g.pause();
        for(int hit=0;hit<5;hit++){
            g.bossX=3450;g.bossY=520;g.bossVY=0;g.bossJump=3;g.bossInvulnerable=0;
            g.x=3470;g.y=g.bossY-DashModel.PH-1;g.vy=250;g.grounded=false;
            g.update(DT,0,false);check(g.bossHP==4-hit,"stomp reduces boss health");
        }
        check(g.shots.isEmpty(),"defeating boss removes attacks");
        g.x=3930;g.update(DT,0,false);check(g.state==DashModel.State.WON,"White House reached after boss defeat");
        g.reset();check(g.state==DashModel.State.READY&&g.bossHP==5&&g.lives==3&&!g.powered,"restart restores stage");
        g=game();g.x=3930;g.update(DT,0,false);check(g.state!=DashModel.State.WON,"cannot win while boss alive");
        // Walking/jumping through the route exercises real physics, including pits and collisions.
        g=game();
        for(int i=0;i<6000;i++){
            if(g.state!=DashModel.State.PLAYING){g.reset();g.start();}
            if(g.grounded&&i%45==0)g.jump();g.update(DT,1,i%2==0);
            check(Double.isFinite(g.x)&&Double.isFinite(g.y)&&(g.health>0||g.state==DashModel.State.OVER),"simulation remains finite");
        }
        SwingUtilities.invokeAndWait(()->{
            try{
                DashModel model=game();DashPanel panel=new DashPanel(model);panel.setSize(1100,760);
                check(panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.SHIFT_DOWN_MASK))!=null,"sprint supports shifted controls");
                panel.pauseForFocusLoss();check(model.state==DashModel.State.PAUSED,"focus loss pauses");
                model.pause();model.x=250;model.update(DT,0,false);model.x=3220;model.update(DT,0,false);
                BufferedImage image=new BufferedImage(1100,760,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics=image.createGraphics();panel.paint(graphics);graphics.dispose();
                check(image.getRGB(0,0)!=image.getRGB(0,735),"world and HUD render");
                ImageIO.write(image,"png",Path.of(System.getProperty("java.io.tmpdir"),"capital-dash-preview.png").toFile());
            }catch(Exception e){throw new RuntimeException(e);}
        });
        checkAudio();
        System.out.println("PASS: movement, jumping, platforms, pause, burgers, boxes, stomps, damage, pits, checkpoints, boss attacks/stomps, victory, restart, sprint input, rendering, 6000-step simulation, growth/ceiling clearance/jump height, synthesized march/effects");
    }

    private static void checkAudio() throws Exception {
        for(DashModel.Cue cue:DashModel.Cue.values()) {
            double energy=0;
            for(int i=0;i<(int)(DashAudio.RATE*DashAudio.duration(cue));i++) {
                double value=DashAudio.effectSample(cue,i/(double)DashAudio.RATE);
                check(Double.isFinite(value)&&Math.abs(value)<1,"effect samples within range");energy+=value*value;
            }
            check(energy>1,"each effect produces sound");
            check(DashAudio.effectSample(cue,DashAudio.duration(cue))==0,"effect ends cleanly");
        }
        byte[] pcm=new byte[DashAudio.RATE*8*2];double energy=0;
        for(int i=0;i<pcm.length/2;i++) {
            double sample=DashAudio.musicSample(i/(double)DashAudio.RATE);
            check(Double.isFinite(sample)&&Math.abs(sample)<1,"music samples within range");
            energy+=sample*sample;
            short value=(short)(sample*26000);pcm[2*i]=(byte)value;pcm[2*i+1]=(byte)(value>>8);
        }
        check(energy>100,"march contains audible signal");
        check(Math.abs(DashAudio.musicSample(0)-DashAudio.musicSample(8))<1e-8,"march loops continuously");
        javax.sound.sampled.AudioFormat format=new javax.sound.sampled.AudioFormat(DashAudio.RATE,16,1,true,false);
        try(var stream=new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(pcm),format,pcm.length/2)) {
            javax.sound.sampled.AudioSystem.write(stream,javax.sound.sampled.AudioFileFormat.Type.WAVE,Path.of(System.getProperty("java.io.tmpdir"),"capital-dash-march.wav").toFile());
        }
        DashAudio audio=new DashAudio();audio.configure(true,false);audio.start();audio.play(DashModel.Cue.JUMP);
        audio.configure(false,true);audio.clearEffects();audio.close();
    }
}
