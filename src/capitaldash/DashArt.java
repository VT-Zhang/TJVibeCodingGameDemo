package capitaldash;

import java.awt.*;
import java.awt.geom.*;

/** Original cartoon portraits and DC scenery, all drawn with Java2D. */
final class DashArt {
    static final Color NAVY=new Color(29,47,82), RED=new Color(209,55,68), CREAM=new Color(255,243,216);
    private DashArt(){}
    static void trump(Graphics2D graphics,double x,double y,boolean right,boolean powered,boolean grown,double time,boolean moving){
        Graphics2D g=(Graphics2D)graphics.create();
        try{
            double size=grown?1.5:1;
            g.translate(x+15*size,y+48*size);g.scale(size,size*48/58);if(!right)g.scale(-1,1);
            if(powered){g.setColor(new Color(255,209,85,70));g.fillOval(-25,-59,50,65);}
            double stride=moving?Math.sin(time*18)*5:0;
            g.setColor(NAVY);g.fillRoundRect(-12,-29,25,25,7,7);
            g.setColor(Color.WHITE);g.fillPolygon(new int[]{-7,8,0},new int[]{-29,-29,-12},3);
            g.setColor(RED);g.fillPolygon(new int[]{0,-3,1,4},new int[]{-27,-20,-12,-20},4);
            g.setColor(new Color(15,26,49));g.fillRoundRect(-12,-9+(int)stride,11,9,3,3);g.fillRoundRect(3,-9-(int)stride,12,9,3,3);
            g.setColor(new Color(236,174,124));g.fillOval(-14,-51,29,28);g.fillOval(9,-37,10,7);
            g.setColor(new Color(246,207,79));g.fillArc(-16,-58,34,24,0,180);
            Path2D hair=new Path2D.Double();hair.moveTo(-16,-45);hair.curveTo(-24,-58,13,-64,18,-49);hair.curveTo(4,-58,-3,-37,-16,-45);g.fill(hair);
            g.setColor(new Color(65,54,45));g.fillRect(6,-42,3,3);g.drawLine(2,-45,10,-44);g.drawLine(6,-30,12,-31);
            g.setColor(NAVY);g.setStroke(new BasicStroke(7,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine(-10,-23,-15,-12);g.drawLine(11,-23,16,-15);
        }finally{g.dispose();}
    }
    static void kamala(Graphics2D graphics,double x,double y,double time){
        Graphics2D g=(Graphics2D)graphics.create();
        try{
            g.translate(x,y);
            g.setColor(new Color(65,36,101));g.fillOval(10,22,76,73);
            g.setColor(new Color(143,98,180));g.setStroke(new BasicStroke(3));g.drawOval(17,29,62,58);
            for(int i=0;i<5;i++){
                int xx=20+i*14;g.setColor(CREAM);g.fillPolygon(new int[]{xx,xx+5,xx+11},new int[]{32,17-(i%2)*6,32},3);
            }
            g.setColor(new Color(82,46,125));g.fillRoundRect(19,51,49,39,14,14);
            g.setColor(new Color(27,22,39));g.fillRoundRect(14,84,28,15,6,6);g.fillRoundRect(52,84,29,15,6,6);
            g.setColor(new Color(49,30,27));g.fillRoundRect(12,7,53,51,25,25);
            g.setColor(new Color(180,121,86));g.fillOval(23,16,34,38);
            g.setColor(new Color(49,30,27));g.fillArc(19,7,44,39,0,180);
            g.setStroke(new BasicStroke(6));g.drawArc(16,10,27,43,75,175);g.drawArc(43,11,24,43,-80,160);
            g.setColor(new Color(41,30,34));g.fillOval(31,31,3,4);g.fillOval(47,31,3,4);
            g.setColor(CREAM);g.fillArc(34,37,16,10,180,180);
            g.setColor(new Color(120,76,160));g.setStroke(new BasicStroke(12,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine(25,61,5,76);g.drawLine(61,61,77,71);
            g.setColor(CREAM);for(int i=0;i<5;i++)g.fillOval(29+i*6,52+(int)(Math.sin(i*Math.PI/4)*4),4,4);
        }finally{g.dispose();}
    }
    static void burger(Graphics2D g,double x,double y){
        g.setColor(new Color(241,180,71));g.fillArc((int)x,(int)y,30,20,0,180);
        g.setColor(CREAM);for(int i=0;i<5;i++)g.fillOval((int)x+5+i*4,(int)y+4+(i%2)*3,2,2);
        g.setColor(new Color(107,62,39));g.fillRoundRect((int)x,(int)y+10,30,5,3,3);
        g.setColor(new Color(111,174,57));g.fillRoundRect((int)x-1,(int)y+14,32,4,3,3);
        g.setColor(new Color(237,178,68));g.fillRoundRect((int)x,(int)y+18,30,4,3,3);
        g.setColor(new Color(107,62,39));g.fillRoundRect((int)x,(int)y+22,30,4,3,3);
        g.setColor(new Color(241,180,71));g.fillRoundRect((int)x,(int)y+26,30,5,4,4);
    }
    static void star(Graphics2D g,double x,double y,double radius){
        Path2D p=new Path2D.Double();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5,r=i%2==0?radius:radius*.45;if(i==0)p.moveTo(x+Math.cos(a)*r,y+Math.sin(a)*r);else p.lineTo(x+Math.cos(a)*r,y+Math.sin(a)*r);}p.closePath();g.fill(p);
    }
    static void whiteHouse(Graphics2D g,int x,int ground){
        g.setColor(new Color(61,102,81));g.fillOval(x-45,ground-35,350,52);
        g.setColor(new Color(235,234,217));g.fillRect(x,ground-131,290,127);
        g.setColor(new Color(253,250,232));g.fillRect(x+10,ground-141,270,18);
        g.setColor(new Color(192,205,205));
        for(int row=0;row<2;row++)for(int col=0;col<11;col++)g.fillRect(x+15+col*24,ground-111+row*48,11,24);
        g.setColor(new Color(255,253,239));g.fillPolygon(new int[]{x+71,x+145,x+219},new int[]{ground-124,ground-169,ground-124},3);
        g.setColor(new Color(220,223,210));g.fillRect(x+70,ground-124,150,8);
        g.setColor(new Color(255,253,239));for(int i=0;i<6;i++)g.fillRect(x+79+i*24,ground-116,9,105);
        g.setColor(new Color(39,58,75));g.fillRoundRect(x+132,ground-53,25,53,8,8);
        g.setColor(new Color(241,239,222));g.fillRect(x+62,ground-12,166,12);
        g.setColor(NAVY);g.drawLine(x+145,ground-170,x+145,ground-210);
        g.setColor(RED);g.fillRect(x+146,ground-210,31,18);g.setColor(Color.WHITE);for(int i=0;i<3;i++)g.fillRect(x+146,ground-207+i*5,31,2);
        g.setColor(NAVY);g.fillRect(x+146,ground-210,12,10);
    }
    static void skyline(Graphics2D g,double camera){
        g.setColor(new Color(171,200,202));
        for(int i=0;i<15;i++){int x=(int)(i*140-camera*.18);g.fillRect(x,440-i%3*15,110,180);}
        int monument=(int)(480-camera*.12);
        g.setColor(new Color(224,226,207));g.fillPolygon(new int[]{monument,monument+16,monument+32,monument+29,monument+3},new int[]{474,270,474,620,620},5);
        int dome=(int)(1050-camera*.16);g.setColor(new Color(224,230,216));g.fillRect(dome-100,480,200,140);g.fillArc(dome-55,389,110,150,0,180);g.fillRect(dome-9,374,18,35);
        for(int i=0;i<9;i++){g.setColor(new Color(161,185,190));g.fillRect(dome-84+i*21,497,8,55);}
    }
    static void text(Graphics2D g,String s,int x,int y,int size,Color c){g.setColor(c);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,size));g.drawString(s,x,y);}
}
