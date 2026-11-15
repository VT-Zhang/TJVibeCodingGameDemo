package alchemy;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

/** Headless rule, persistence, mouse interaction, and rendering checks. */
public final class AlchemyCheck {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        AlchemyModel game=new AlchemyModel(null);
        check(game.discovered.size()==6,"six starter elements");
        check(game.combine("City","No such item")==null,"unknown ingredients rejected");
        check(game.combine("City","Snow").result().equals("Oslo"),"city recipe");
        check(!game.combine("Snow","City").fresh(),"order independent and no duplicate discovery");
        check(game.combine("City","Water")==null,"failed recipe changes nothing");
        check(game.combine("City","City").result().equals("Metropolis"),"self combinations supported");
        check(game.library("osl","Cities").size()==1 && game.library("osl","Sports").isEmpty(),"search and category filters");
        int iterations=0;
        while(game.hint()!=null){
            AlchemyModel.Recipe r=game.hint();check(game.combine(r.first(),r.second()).fresh(),"hint leads to discovery");
            check(++iterations<200,"recipe graph progresses");
        }
        check(game.discovered.size()==game.elements.size(),"every discovery reachable from starters");
        check(game.discovered.contains("City of Champions"),"final goal reachable");
        for(AlchemyModel.Recipe r:game.recipes)check(game.combine(r.second(),r.first()).result().equals(r.result()),"all recipes reversible");
        Path directory=Files.createTempDirectory("city-sport-alchemy-");Path save=directory.resolve("save");
        try{
            AlchemyModel persistent=new AlchemyModel(save);persistent.combine("Ball","City");
            check(new AlchemyModel(save).discovered.contains("Basketball"),"progress saved and loaded");
            Files.writeString(save,"Unknown\nCity\nBasketball\n");
            check(new AlchemyModel(save).discovered.size()==7,"unknown saved entries ignored, starters restored");
            persistent.reset();check(new AlchemyModel(save).discovered.size()==6,"reset persists");
        }finally{Files.deleteIfExists(save);Files.deleteIfExists(directory);}
        SwingUtilities.invokeAndWait(()->{
            try{
                AlchemyModel model=new AlchemyModel(null);AlchemyPanel panel=new AlchemyPanel(model);
                panel.setSize(1160,780);layout(panel);
                AlchemyPanel.Board board=panel.board;
                AlchemyPanel.Token a=board.tokens.get(0),b=board.tokens.get(1);
                click(board,a.x+20,a.y+20);click(board,b.x+20,b.y+20);
                check(model.discovered.contains("Basketball")&&board.tokens.size()==1,"click-two-cards combines");
                board.tokens.clear();board.addToken("Land",100,150);board.dropLibrary("Ball",100,150);
                check(model.discovered.contains("Soccer")&&board.tokens.size()==1,"library drop combines");
                board.addToken("City",350,150);
                AlchemyPanel.Token token=board.tokens.get(0);
                board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_PRESSED,0,0,token.x+10,token.y+10,1,false,MouseEvent.BUTTON3));
                check(board.tokens.size()==1,"right-click removes a board card without losing discovery");
                board.tokens.clear();board.addToken("City",90,150);board.addToken("Ball",350,150);
                board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_PRESSED,0,0,100,160,1,false,MouseEvent.BUTTON1));
                board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_DRAGGED,0,MouseEvent.BUTTON1_DOWN_MASK,360,160,0,false,MouseEvent.NOBUTTON));
                board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_RELEASED,0,0,360,160,1,false,MouseEvent.BUTTON1));
                check(board.tokens.size()==1&&board.tokens.get(0).name.equals("Basketball"),"board dragging combines");
                board.addToken("City",45,280);board.addToken("Snow",300,280);board.dropLibrary("City",300,280);
                BufferedImage image=new BufferedImage(1160,780,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics=image.createGraphics();panel.paint(graphics);graphics.dispose();
                check(image.getRGB(15,15)!=image.getRGB(15,500),"header and workbench render");
                ImageIO.write(image,"png",Path.of(System.getProperty("java.io.tmpdir"),"city-sport-alchemy-preview.png").toFile());
            }catch(Exception e){throw new RuntimeException(e);}
        });
        System.out.println("PASS: "+game.elements.size()+" reachable elements, "+game.recipes.size()+" recipes, order independence, hints, filters, persistence, reset, click/drag/drop/remove, rendering");
    }
    private static void click(AlchemyPanel.Board board,int x,int y){
        board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_PRESSED,0,0,x,y,1,false,MouseEvent.BUTTON1));
        board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_RELEASED,0,0,x,y,1,false,MouseEvent.BUTTON1));
    }
    private static void layout(java.awt.Container container){container.doLayout();for(java.awt.Component child:container.getComponents())if(child instanceof java.awt.Container nested)layout(nested);}
}
