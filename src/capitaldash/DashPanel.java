package capitaldash;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import static capitaldash.DashArt.*;

/** Scrolling view and input, separated from the platforming rules. */
final class DashPanel extends JPanel {
    private static final long serialVersionUID=1L;
    static final int WIDTH=1100,HEIGHT=760;
    final transient DashModel game;
    private final transient Set<Integer> keys=new HashSet<>();
    private final Timer timer;
    final transient DashAudio audio=new DashAudio();
    private boolean muted;
    private long previous;
    private double accumulator;
    DashPanel(DashModel game){
        this.game=game;setPreferredSize(new Dimension(WIDTH,HEIGHT));setBackground(NAVY);
        int[] codes={KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_A,KeyEvent.VK_D,KeyEvent.VK_UP,KeyEvent.VK_W,KeyEvent.VK_SPACE,KeyEvent.VK_SHIFT,KeyEvent.VK_ENTER,KeyEvent.VK_P,KeyEvent.VK_R,KeyEvent.VK_M};
        for(int code:codes)for(boolean release:new boolean[]{false,true}){
            String name=code+":"+release;
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(code,0,release),name);
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(code,InputEvent.SHIFT_DOWN_MASK,release),name);
            getActionMap().put(name,new AbstractAction(){public void actionPerformed(ActionEvent e){
                if(release){keys.remove(code);return;}if(!keys.add(code))return;
                switch(code){
                    case KeyEvent.VK_SPACE,KeyEvent.VK_UP,KeyEvent.VK_W->game.jump();
                    case KeyEvent.VK_ENTER->{if(game.state==DashModel.State.OVER||game.state==DashModel.State.WON){game.reset();audio.clearEffects();}game.start();}
                    case KeyEvent.VK_R->{game.reset();audio.clearEffects();keys.clear();}
                    case KeyEvent.VK_P->{game.pause();audio.clearEffects();keys.clear();}
                    case KeyEvent.VK_M->muted=!muted;
                    default->{}
                }
                syncAudio();repaint();
            }});
        }
        timer=new Timer(16,e->tick());
    }
    @Override public void addNotify(){super.addNotify();previous=System.nanoTime();audio.start();timer.start();}
    @Override public void removeNotify(){timer.stop();audio.close();super.removeNotify();}
    void pauseForFocusLoss(){if(game.state==DashModel.State.PLAYING)game.pause();keys.clear();audio.clearEffects();syncAudio();repaint();}
    private void tick(){
        long now=System.nanoTime();accumulator+=Math.min(.1,(now-previous)/1e9);previous=now;
        while(accumulator>=1.0/120){int direction=(keys.contains(KeyEvent.VK_RIGHT)||keys.contains(KeyEvent.VK_D)?1:0)-(keys.contains(KeyEvent.VK_LEFT)||keys.contains(KeyEvent.VK_A)?1:0);game.update(1.0/120,direction,keys.contains(KeyEvent.VK_SHIFT));accumulator-=1.0/120;}
        syncAudio();repaint();
    }
    private void syncAudio(){
        audio.configure(game.state==DashModel.State.PLAYING,muted);
        for(DashModel.Cue cue:game.drainSounds())audio.play(cue);
    }
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
        try{
            double scale=Math.min(getWidth()/(double)WIDTH,getHeight()/(double)HEIGHT);
            g.translate((getWidth()-WIDTH*scale)/2,(getHeight()-HEIGHT*scale)/2);g.scale(scale,scale);g.setClip(0,0,WIDTH,HEIGHT);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            double camera=Math.max(0,Math.min(DashModel.WORLD-WIDTH,game.x-340));
            g.setPaint(new GradientPaint(0,0,new Color(114,194,222),0,650,new Color(235,241,206)));g.fillRect(0,0,WIDTH,HEIGHT);
            g.setColor(new Color(255,235,164));g.fillOval(890,105,82,82);
            g.setColor(new Color(255,255,255,155));for(int i=0;i<7;i++){int cx=(int)(i*240-camera*.23-game.time*7)%1500;if(cx< -120)cx+=1500;g.fillRoundRect(cx,135+i%3*48,130,28,28,28);g.fillOval(cx+30,113+i%3*48,60,50);}
            skyline(g,camera);
            g.setColor(new Color(87,155,164));g.fillRect(0,620,WIDTH,100);
            Graphics2D world=(Graphics2D)g.create();world.translate(-camera,0);
            drawWorld(world);world.dispose();
            drawHud(g);
            if(game.state!=DashModel.State.PLAYING)overlay(g);
        }finally{g.dispose();}
    }
    private void drawWorld(Graphics2D g){
        whiteHouse(g,3810,620);
        for(DashModel.Platform p:game.platforms){
            int x=(int)p.x,y=(int)p.y,w=(int)p.w,h=(int)p.h;
            if(p.box){g.setColor(p.used?new Color(153,135,111):new Color(237,183,66));g.fillRoundRect(x,y,w,h,6,6);g.setColor(new Color(120,84,50));g.setStroke(new BasicStroke(2));g.drawRoundRect(x+2,y+2,w-4,h-4,5,5);text(g,p.used?"•":"?",x+13,y+28,23,CREAM);}
            else if(p.y==DashModel.FLOOR){
                g.setColor(new Color(167,119,81));g.fillRect(x,y,w,h);g.setColor(new Color(100,156,83));g.fillRect(x,y,w,12);g.setColor(new Color(206,157,103));
                for(int bx=x;bx<x+w;bx+=40)for(int by=y+23;by<720;by+=30)g.fillRect(bx+7,by,20,4);
            }else{
                g.setColor(new Color(181,95,75));g.fillRoundRect(x,y,w,h,4,4);g.setColor(new Color(237,174,118));for(int bx=x+8;bx<x+w;bx+=28)g.drawLine(bx,y+3,bx,y+h-3);
                g.setColor(CREAM);g.drawLine(x+3,y+3,x+w-3,y+3);
            }
        }
        flag(g,1810,"CHECKPOINT",game.checkpoint>=1810);flag(g,3140,"BOSS ARENA",game.bossActive);
        text(g,"NATIONAL MALL  →",1760,340,15,NAVY);
        text(g,"THE WHITE HOUSE",3822,355,21,NAVY);
        for(DashModel.Pickup p:game.pickups)if(!p.collected){if(p.burger)burger(g,p.x,p.y+Math.sin(game.time*4)*3);else{g.setColor(new Color(255,207,64));star(g,p.x+9,p.y+11+Math.sin(game.time*4+p.x)*3,11);}}
        for(DashModel.Enemy e:game.enemies)if(e.alive){
            int x=(int)e.x,y=(int)e.y;g.setColor(new Color(109,72,60));g.fillRoundRect(x,y,34,27,11,11);g.setColor(new Color(48,42,46));g.fillOval(x-3,y+23,16,8);g.fillOval(x+21,y+23,16,8);g.setColor(Color.WHITE);g.fillOval(x+7,y+7,7,10);g.fillOval(x+22,y+7,7,10);g.setColor(NAVY);g.fillOval(x+9,y+10,3,4);g.fillOval(x+23,y+10,3,4);
        }
        if(game.bossHP>0){
            if(game.bossInvulnerable==0||(int)(game.time*15)%2==0)kamala(g,game.bossX,game.bossY,game.time);
            text(g,"KAMALA",(int)game.bossX-3,(int)game.bossY-15,14,new Color(93,52,129));
        }
        for(DashModel.Shot shot:game.shots){g.setColor(new Color(151,92,180));g.fillOval((int)shot.x-13,(int)shot.y-13,26,26);text(g,"!",(int)shot.x-3,(int)shot.y+6,18,Color.WHITE);}
        if(game.invulnerable==0||(int)(game.time*14)%2==0)trump(g,game.x,game.y,game.facingRight,game.powered,game.grown,game.time,Math.abs(game.vx)>0&&game.grounded);
        if(game.bossActive&&game.bossHP>0){g.setColor(new Color(198,60,79,100));g.fillRect(3090,390,12,230);}
    }
    private void flag(Graphics2D g,int x,String label,boolean active){g.setColor(NAVY);g.fillRect(x,520,4,100);g.setColor(active?new Color(74,164,122):CREAM);g.fillPolygon(new int[]{x+4,x+70,x+4},new int[]{520,540,560},3);text(g,label,x-20,505,11,NAVY);}
    private void drawHud(Graphics2D g){
        g.setColor(new Color(24,41,70,235));g.fillRoundRect(20,18,1060,72,18,18);
        text(g,"CAPITAL DASH",39,48,21,Color.WHITE);text(g,"WASHINGTON, DC  /  STAGE 01",40,72,11,new Color(177,209,220));
        for(int i=0;i<game.maxHealth;i++){g.setColor(i<game.health?new Color(247,105,115):new Color(75,88,109));int x=340+i*26;g.fillOval(x,38,12,12);g.fillOval(x+9,38,12,12);g.fillPolygon(new int[]{x,x+21,x+10},new int[]{45,45,61},3);}
        text(g,muted?"AUDIO OFF":audio.unavailable()?"AUDIO UNAVAILABLE":"♪ MARCH",812,74,10,new Color(177,209,220));
        text(g,"LIVES  "+game.lives,510,52,16,CREAM);g.setColor(new Color(255,207,64));star(g,660,47,11);text(g,String.format("%03d",game.coins),680,53,19,CREAM);
        text(g,game.powered?"BIG MAC POWER":"TRUMP / PLAYER 1",812,52,16,game.powered?new Color(255,207,64):Color.WHITE);
        if(game.bossActive&&game.bossHP>0){g.setColor(new Color(24,41,70,210));g.fillRoundRect(660,103,420,43,12,12);text(g,"BOSS / KAMALA",677,130,13,CREAM);for(int i=0;i<5;i++){g.setColor(i<game.bossHP?new Color(177,119,212):new Color(66,67,95));g.fillRoundRect(823+i*46,117,37,12,5,5);}}
        g.setColor(NAVY);g.fillRect(0,700,WIDTH,60);text(g,game.message,24,723,13,CREAM);text(g,"← → / A D   MOVE     SPACE / ↑ / W   JUMP     SHIFT   SPRINT     P   PAUSE     R   RESTART     M   MUTE",24,748,12,new Color(172,199,213));
    }
    private void overlay(Graphics2D g){
        g.setColor(new Color(13,26,48,150));g.fillRect(0,90,WIDTH,610);
        g.setColor(new Color(255,249,231));g.fillRoundRect(235,180,630,365,28,28);
        String title=switch(game.state){case READY->"CAPITAL DASH";case PAUSED->"QUICK RECESS";case WON->"STAGE 1 CLEAR!";default->"CAMPAIGN OVER";};
        text(g,title,285,239,34,NAVY);
        trump(g,306,315,true,true,true,0,false);burger(g,390,305);kamala(g,696,280,0);
        text(g,"A cartoon platforming parody in Washington, DC",285,278,15,new Color(99,113,121));
        text(g,game.state==DashModel.State.WON?"You reached the White House. Nice jumping!":"Grab a Big Mac. Cross the Mall. Reach the White House.",285,414,16,NAVY);
        text(g,"Stomp enemies and Kamala's shell from above.",285,444,15,NAVY);
        text(g,game.state==DashModel.State.PAUSED?"P TO RESUME":"ENTER TO "+(game.state==DashModel.State.READY?"START":"PLAY AGAIN"),285,497,23,RED);
        text(g,"Original fan-made artwork • Fictional, playful political caricatures",285,524,11,new Color(122,126,126));
    }
}
