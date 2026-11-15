package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Keyboard input and Java2D view, with a fixed simulation step. */
final class TetrisPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int WIDTH = 1280, HEIGHT = 860, BX = 320, BY = 100, CELL = 34;
    private static final Color NAVY = new Color(10, 22, 45), RED = new Color(225, 60, 83),
            WHITE = new Color(237, 243, 253), MUTED = new Color(151, 172, 201);
    final transient TetrisModel game;
    private final transient Set<Integer> keys = new HashSet<>();
    private final javax.swing.Timer timer;
    private final transient Path bestFile;
    private int best;
    private long previous;
    private double accumulator, horizontalTime, downTime;
    private int direction;

    TetrisPanel(TetrisModel game, Path bestFile) {
        this.game = game; this.bestFile = bestFile;
        if (bestFile != null) try { best = Math.max(0, Integer.parseInt(Files.readString(bestFile).trim())); }
        catch (IOException | NumberFormatException ignored) { best = 0; }
        setPreferredSize(new Dimension(WIDTH, HEIGHT)); setBackground(NAVY);
        int[] codes = {KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_DOWN,KeyEvent.VK_UP,
            KeyEvent.VK_X,KeyEvent.VK_Z,KeyEvent.VK_SPACE,KeyEvent.VK_C,KeyEvent.VK_P,
            KeyEvent.VK_R,KeyEvent.VK_ENTER,KeyEvent.VK_1,KeyEvent.VK_2,KeyEvent.VK_3};
        for (int code : codes) {
            bind(code, false); bind(code, true);
        }
        timer = new javax.swing.Timer(16, e -> tick());
    }
    private void bind(int code, boolean release) {
        String name = code + ":" + release;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(code, 0, release), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                if (release) { keys.remove(code); return; }
                if (!keys.add(code)) return;
                if (code == KeyEvent.VK_R) { game.reset(); clearInput(); }
                else if (code == KeyEvent.VK_P) { game.togglePause(); clearInput(); }
                else if (code == KeyEvent.VK_ENTER) {
                    if (game.state == TetrisModel.State.OVER) game.reset();
                    game.start();
                } else if (game.state == TetrisModel.State.PLAYING) {
                    switch (code) {
                        case KeyEvent.VK_LEFT -> { game.move(-1); direction = -1; horizontalTime = -.16; }
                        case KeyEvent.VK_RIGHT -> { game.move(1); direction = 1; horizontalTime = -.16; }
                        case KeyEvent.VK_DOWN -> { game.softDrop(); downTime = 0; }
                        case KeyEvent.VK_UP, KeyEvent.VK_X -> game.rotate(1);
                        case KeyEvent.VK_Z -> game.rotate(-1);
                        case KeyEvent.VK_SPACE -> game.hardDrop();
                        case KeyEvent.VK_C -> game.hold();
                        case KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3 -> game.power(code - KeyEvent.VK_1);
                        default -> { }
                    }
                }
                repaint();
            }
        });
    }
    private void clearInput() { keys.clear(); direction = 0; horizontalTime = downTime = 0; }
    void pauseForFocusLoss() {
        if (game.state == TetrisModel.State.PLAYING) game.togglePause();
        clearInput(); repaint();
    }
    @Override public void addNotify() { super.addNotify(); previous = System.nanoTime(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }
    private void tick() {
        long now = System.nanoTime();
        accumulator += Math.min(.1, (now - previous) / 1e9); previous = now;
        while (accumulator >= 1.0 / 120) { step(1.0 / 120); accumulator -= 1.0 / 120; }
        if (game.state == TetrisModel.State.OVER && game.score > best) {
            best = game.score;
            if (bestFile != null) try { Files.writeString(bestFile, Integer.toString(best)); }
            catch (IOException ignored) { /* Best remains available for this session. */ }
        }
        repaint();
    }
    private void step(double dt) {
        if (game.state == TetrisModel.State.PLAYING) {
            int heldDirection = keys.contains(KeyEvent.VK_RIGHT) ? 1 : keys.contains(KeyEvent.VK_LEFT) ? -1 : 0;
            if (heldDirection != direction) { direction = heldDirection; horizontalTime = -.16; }
            if (direction != 0) {
                horizontalTime += dt;
                if (horizontalTime >= .055) { game.move(direction); horizontalTime = 0; }
            }
            if (keys.contains(KeyEvent.VK_DOWN)) {
                downTime += dt;
                if (downTime >= .035) { game.softDrop(); downTime = 0; }
            }
            game.update(dt);
        }
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            double scale = Math.min(getWidth() / (double)WIDTH, getHeight() / (double)HEIGHT);
            g.translate((getWidth() - WIDTH * scale) / 2, (getHeight() - HEIGHT * scale) / 2);
            g.scale(scale, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0,0,new Color(21,41,73),WIDTH,860,NAVY));
            g.fillRect(0,0,WIDTH,HEIGHT);
            g.setColor(RED); g.fillRect(30,30,5,44);
            text(g,"TJHSST / ANIMAL TETRIS",48,52,25,WHITE);
            text(g,"THOMAS JEFFERSON HIGH SCHOOL FOR SCIENCE AND TECHNOLOGY",48,74,11,MUTED);
            text(g,"GO COLONIALS!",785,53,17,RED);
            card(g,30,100,265,205);
            text(g,"HABITAT REPORT",48,129,12,MUTED);
            text(g,String.format("%07d",game.score),48,178,35,WHITE);
            text(g,"BEST  " + Math.max(best,game.score),48,207,14,MUTED);
            text(g,"LEVEL  " + game.level() + "      LINES  " + game.lines,48,242,16,WHITE);
            text(g,"1 charge earned every 3 lines",48,280,12,MUTED);
            card(g,30,323,265,150);
            text(g,"HOLD / C",48,351,12,MUTED);
            preview(g,game.held,78,366,25);
            text(g,game.held == null ? "Save a species for later" : game.held.animal,48,452,14,WHITE);
            Graphics2D characterGraphics = (Graphics2D) g.create();
            drawTroublemaker(characterGraphics);
            characterGraphics.dispose();
            g.setColor(new Color(4,12,27)); g.fillRoundRect(BX-5,BY-5,350,690,12,12);
            g.setColor(new Color(32,49,77));
            for (int r=0;r<=20;r++) g.drawLine(BX,BY+r*CELL,BX+340,BY+r*CELL);
            for (int c=0;c<=10;c++) g.drawLine(BX+c*CELL,BY,BX+c*CELL,BY+680);
            for (int r=0;r<20;r++) for (int c=0;c<10;c++)
                if (game.board[r][c]!=null) AnimalArt.block(g,game.board[r][c],BX+c*CELL,BY+r*CELL,CELL,false);
            if (game.state != TetrisModel.State.OVER) {
                piece(g,game.ghostY(),true); piece(g,game.y,false);
            }
            card(g,685,100,285,216);
            text(g,"NEXT IN THE HABITAT",705,129,12,MUTED);
            int i=0;
            for (Tetromino type : game.queue) {
                if (i==3) break;
                preview(g,type,701,142+i*53,17);
                text(g,type.animal,792,171+i*53,14,WHITE); i++;
            }
            card(g,685,334,285,223);
            text(g,"COLONIAL LAB / POWER-UPS",705,364,12,RED);
            String[] names={"1  OWL RESCUE", "2  AXOLOTL CHILL", "3  FOX BURST"};
            String[] hints={"Remove lowest occupied row", "Slow gravity for 12 seconds", "Clear 3×3 at the ghost landing"};
            for (i=0;i<3;i++) {
                text(g,names[i]+"   ×"+game.powers[i],705,398+i*54,15,WHITE);
                text(g,hints[i],705,418+i*54,12,MUTED);
            }
            card(g,995,575,255,205);
            text(g,"MISSION CONTROLS",1013,606,12,MUTED);
            String[] controls={"← →   Move     ↓   Soft drop", "↑ / X   Rotate     Z   Reverse", "SPACE   Hard drop     C   Hold", "1 / 2 / 3   Activate power-up", "P   Pause     R   New game"};
            for(i=0;i<controls.length;i++) text(g,controls[i],1013,638+i*26,13,WHITE);
            text(g,game.message,32,819,15,WHITE);
            if (game.slowRemaining>0) text(g,String.format("CHILL / %.1fs",game.slowRemaining),777,819,15,new Color(104,211,228));
            Graphics2D timothyGraphics = (Graphics2D) g.create();
            drawTimothy(timothyGraphics);
            timothyGraphics.dispose();
            drawThrow(g);
            drawVacuum(g);
            if (game.state != TetrisModel.State.PLAYING) overlay(g);
        } finally { g.dispose(); }
    }
    private void drawTroublemaker(Graphics2D g) {
        card(g,30,492,265,288);
        text(g,"THE SUIT / UNINVITED GUEST",48,521,12,RED);
        boolean throwing = game.throwFlight > 0;
        boolean warning = game.throwColumn >= 0;
        // An original cartoon businessperson: navy jacket, white shirt, red tie, briefcase.
        g.setColor(new Color(230,191,153)); g.fillRoundRect(109,580,22,28,8,8);
        g.setColor(new Color(53,69,94)); g.fillRoundRect(79,601,82,86,20,20);
        g.setColor(WHITE); g.fillPolygon(new int[]{101,139,120},new int[]{602,602,659},3);
        g.setColor(RED); g.fillPolygon(new int[]{120,114,119,125},new int[]{608,622,650,622},4);
        g.setColor(new Color(32,46,69));
        g.fillRoundRect(85,675,27,52,8,8);g.fillRoundRect(130,675,27,52,8,8);
        g.setColor(new Color(7,15,30));g.fillRoundRect(78,717,36,13,7,7);g.fillRoundRect(129,717,36,13,7,7);
        g.setColor(new Color(244,210,174));g.fillOval(96,542,48,52);
        g.setColor(new Color(70,48,40));g.fillArc(94,535,52,38,0,180);
        g.setColor(NAVY);g.setStroke(new BasicStroke(2));
        g.drawRoundRect(101,558,16,12,4,4);g.drawRoundRect(123,558,16,12,4,4);g.drawLine(117,563,123,563);
        g.drawArc(114,574,18,8,190,150);
        g.setColor(new Color(53,69,94));g.setStroke(new BasicStroke(17,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.drawLine(87,615,65,654);
        g.drawLine(153,614,throwing?198:180,throwing?562:596);
        g.setColor(new Color(244,210,174));g.fillOval(throwing?192:174,throwing?550:584,16,16);
        g.setColor(new Color(131,88,59));g.fillRoundRect(44,659,44,32,5,5);
        g.setStroke(new BasicStroke(3));g.drawRoundRect(55,650,22,15,5,5);
        if (!throwing) AnimalArt.block(g,game.thrownAnimal,176,561,25,false);
        text(g,warning?"Catch this!":"More work",174,631,13,warning?RED:WHITE);
        text(g,warning?"INCOMING":"for you.",174,651,13,warning?RED:WHITE);
        String status = throwing?"DELIVERY IN FLIGHT!":String.format("Next nuisance in %.0fs",Math.ceil(game.throwCountdown));
        text(g,status,48,758,14,warning?RED:MUTED);
    }
    private void drawThrow(Graphics2D g) {
        if(game.throwColumn<0)return;
        int targetX=BX+game.throwColumn*CELL;
        g.setColor(new Color(225,60,83,35));g.fillRect(targetX,BY,CELL,680);
        text(g,"!",targetX+12,BY+20,17,RED);
        if(game.throwFlight<=0)return;
        double t=1-game.throwFlight/TetrisModel.THROW_FLIGHT;
        double targetY=BY+Math.max(0,game.throwRow)*CELL;
        double px=200+(targetX-200)*t;
        double py=556+(targetY-556)*t-240*Math.sin(Math.PI*t);
        Graphics2D flying=(Graphics2D)g.create();
        flying.translate(px+17,py+17);flying.rotate(t*Math.PI*3);
        AnimalArt.block(flying,game.thrownAnimal,-17,-17,CELL,false);flying.dispose();
    }
    private void drawTimothy(Graphics2D g) {
        Color teal = new Color(101,214,206);
        card(g,995,100,255,457);
        text(g,"TIMOTHY / CLEANUP CREW",1013,130,13,teal);
        boolean sucking = game.vacuumFlight > 0;
        boolean warming = game.vacuumCountdown <= 2;
        text(g,sucking?"WHOOSH!":warming?"Vacuum warming up...":"Leave the mess to me.",1013,163,14,WHITE);
        // Friendly cleanup crew member in a TJ hoodie, gripping a vacuum hose.
        g.setColor(new Color(194,139,99));g.fillRoundRect(1121,286,22,28,8,8);
        g.setColor(new Color(170,45,68));g.fillRoundRect(1088,305,85,105,24,24);
        g.setColor(WHITE);text(g,"TJ",1114,347,23,WHITE);
        g.setColor(new Color(27,46,74));g.fillRoundRect(1095,400,27,72,8,8);g.fillRoundRect(1140,400,27,72,8,8);
        g.setColor(new Color(5,13,28));g.fillRoundRect(1086,464,39,15,8,8);g.fillRoundRect(1137,464,39,15,8,8);
        g.setColor(new Color(206,155,115));g.fillOval(1106,241,51,59);
        g.setColor(new Color(49,33,28));g.fillArc(1103,231,57,47,0,180);
        for(int i=0;i<4;i++)g.fillOval(1105+i*12,231-(i%2)*6,21,22);
        g.setColor(NAVY);g.fillOval(1118,265,4,5);g.fillOval(1141,265,4,5);
        g.setStroke(new BasicStroke(2));g.drawArc(1120,277,22,12,190,150);
        // Canister, wheels, flexible hose and left-facing suction nozzle.
        g.setColor(new Color(17,29,48));g.setStroke(new BasicStroke(15,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        java.awt.geom.Path2D hose = new java.awt.geom.Path2D.Double();
        hose.moveTo(1181,430); hose.curveTo(1230,535,1020,520,1044,365);
        g.draw(hose);
        g.setColor(new Color(112,136,157));g.setStroke(new BasicStroke(8,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.drawLine(1044,365,1081,406);
        g.setColor(teal);g.fillRoundRect(1169,416,53,62,18,18);
        g.setColor(new Color(37,103,114));g.fillRoundRect(1175,432,41,13,5,5);
        g.setColor(NAVY);g.fillOval(1174,469,17,17);g.fillOval(1203,469,17,17);
        g.setColor(new Color(179,196,212));
        g.fillPolygon(new int[]{1014,1044,1058,1014},new int[]{344,353,376,389},4);
        g.setColor(new Color(17,39,56));g.fillRoundRect(1008,343,12,49,5,5);
        g.setColor(new Color(170,45,68));g.setStroke(new BasicStroke(18,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.drawLine(1097,321,1061,355);g.drawLine(1161,325,1176,393);
        g.setColor(new Color(206,155,115));g.fillOval(1050,347,18,17);g.fillOval(1169,388,17,18);
        if(sucking || warming) {
            g.setColor(new Color(101,214,206,sucking?180:85));g.setStroke(new BasicStroke(2));
            double phase=sucking?1-game.vacuumFlight/TetrisModel.VACUUM_FLIGHT:1-game.vacuumCountdown/2;
            for(int i=0;i<3;i++) {
                int xx=966+(int)((phase*36+i*13)%38);
                g.drawArc(xx,345-i*5,27,42+i*9,-70,140);
            }
        }
        text(g,sucking?"SUCTION ACTIVE":String.format("Next cleanup in %.0fs",Math.ceil(game.vacuumCountdown)),1013,516,14,teal);
        text(g,"Blocks collected: "+game.blocksVacuumed,1013,540,12,MUTED);
        card(g,685,575,285,205);
        text(g,"THE SIDELINE RIVALRY",705,605,12,MUTED);
        text(g,"The Suit adds trouble.",705,640,16,RED);
        text(g,"Timothy tidies it up.",705,670,16,teal);
        text(g,"Every cleanup removes up to",705,711,13,WHITE);
        text(g,"3 settled animal squares.",705,734,13,WHITE);
        text(g,"Your falling piece stays yours.",705,760,12,MUTED);
    }
    private void drawVacuum(Graphics2D g) {
        if(game.vacuumFlight<=0)return;
        double t=1-game.vacuumFlight/TetrisModel.VACUUM_FLIGHT;
        Graphics2D effect=(Graphics2D)g.create();
        try {
            for(TetrisModel.VacuumBlock block:game.vacuumBlocks) {
                double startX=BX+block.column()*CELL+CELL/2.0;
                double startY=BY+block.row()*CELL+CELL/2.0;
                double px=startX+(1014-startX)*t;
                double py=startY+(367-startY)*t-90*Math.sin(Math.PI*t);
                effect.setColor(new Color(101,214,206,55));
                effect.setStroke(new BasicStroke(2));effect.drawLine((int)startX,(int)startY,(int)px,(int)py);
                Graphics2D animal=(Graphics2D)effect.create();
                animal.translate(px,py);animal.rotate(t*Math.PI*4);
                double size=1-.8*t;animal.scale(size,size);
                AnimalArt.block(animal,block.animal(),-CELL/2,-CELL/2,CELL,false);animal.dispose();
            }
        } finally { effect.dispose(); }
    }
    private void overlay(Graphics2D g) {
        g.setColor(new Color(4,12,27,225)); g.fillRoundRect(BX,BY,340,680,10,10);
        String title = switch(game.state) {
            case READY -> "BUILD. RESCUE. REPEAT.";
            case PAUSED -> "HABITAT PAUSED";
            default -> "GREAT FIELDWORK!";
        };
        AnimalArt.block(g,Tetromino.Z,443,270,90,false);
        centered(g,title,410,20,WHITE);
        centered(g,game.state==TetrisModel.State.OVER ? "SCORE  "+game.score : "Animal blocks. Colonial spirit.",448,16,MUTED);
        centered(g,game.state==TetrisModel.State.PAUSED ? "P TO RESUME" : "ENTER TO "+(game.state==TetrisModel.State.OVER ? "TRY AGAIN" : "START"),502,18,RED);
        centered(g,"Complete rows to restore habitats.",548,13,MUTED);
        centered(g,"Use the lab when the stack gets wild.",572,13,MUTED);
    }
    private void centered(Graphics2D g,String s,int y,int size,Color color) {
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,size));
        text(g,s,BX+(340-g.getFontMetrics().stringWidth(s))/2,y,size,color);
    }
    private void piece(Graphics2D g,int py,boolean ghost) {
        for(int[] c:game.active.cells(game.rotation))
            AnimalArt.block(g,game.active,BX+(game.x+c[0])*CELL,BY+(py+c[1])*CELL,CELL,ghost);
    }
    private static void preview(Graphics2D g,Tetromino type,int x,int y,int size) {
        if(type==null)return;
        for(int[] c:type.cells(0)) AnimalArt.block(g,type,x+c[0]*size,y+c[1]*size,size,false);
    }
    private static void card(Graphics2D g,int x,int y,int w,int h) {
        g.setColor(new Color(22,40,68));g.fillRoundRect(x,y,w,h,16,16);
        g.setColor(new Color(46,65,94));g.drawRoundRect(x,y,w,h,16,16);
    }
    private static void text(Graphics2D g,String s,int x,int y,int size,Color color) {
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,size));g.setColor(color);g.drawString(s,x,y);
    }
}
