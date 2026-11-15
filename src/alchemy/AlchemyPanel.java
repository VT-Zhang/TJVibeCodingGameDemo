package alchemy;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/** Reusable discovery library beside a drag-and-drop combination workbench. */
final class AlchemyPanel extends JPanel {
    private static final long serialVersionUID=1L;
    static final Color INK=new Color(25,43,61), MUTED=new Color(105,122,131),
        TEAL=new Color(24,130,125), PAPER=new Color(246,247,241), CORAL=new Color(223,116,81);
    final transient AlchemyModel model;
    final Board board=new Board();
    private final DefaultListModel<AlchemyModel.Element> listModel=new DefaultListModel<>();
    private final JList<AlchemyModel.Element> library=new JList<>(listModel);
    private final JTextField search=new JTextField();
    private final JComboBox<String> category=new JComboBox<>(new String[]{"All","Basics","Places","Sports","Culture","Cities","Events"});
    private final JLabel progress=new JLabel(), status=new JLabel();

    AlchemyPanel(AlchemyModel model) {
        this.model=model;
        setLayout(new BorderLayout(0,0));setBackground(PAPER);setPreferredSize(new Dimension(1160,780));
        JPanel header=new JPanel(new BorderLayout(20,12));header.setBackground(INK);header.setBorder(BorderFactory.createEmptyBorder(23,28,22,28));
        JLabel title=new JLabel("CITY × SPORT  /  ALCHEMY");title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,27));title.setForeground(Color.WHITE);
        JLabel sub=new JLabel("Small combinations. Big discoveries. Build your own world of play.");sub.setForeground(new Color(173,201,207));
        JPanel titlePanel=new JPanel(new GridLayout(2,1,0,7));titlePanel.setOpaque(false);titlePanel.add(title);titlePanel.add(sub);
        header.add(titlePanel,BorderLayout.CENTER);progress.setForeground(new Color(125,224,205));progress.setFont(new Font(Font.SANS_SERIF,Font.BOLD,17));header.add(progress,BorderLayout.EAST);
        add(header,BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(18,0));center.setBackground(PAPER);center.setBorder(BorderFactory.createEmptyBorder(18,20,12,20));
        JPanel workshop=new JPanel(new BorderLayout(0,10));workshop.setOpaque(false);
        JPanel tools=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));tools.setOpaque(false);
        tools.add(button("Hint",this::hint));tools.add(button("Recipe journal",this::journal));
        tools.add(button("Clear board",()->{board.tokens.clear();board.selected=null;board.repaint();say("A clean slate. Your discoveries are safe in the collection.");}));
        tools.add(button("New collection",()->{
            if(JOptionPane.showConfirmDialog(this,"Erase all discoveries and start again?","New collection",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                model.reset();board.tokens.clear();board.selected=null;search.setText("");category.setSelectedItem("All");refresh();seedBoard();say("New adventure! Combine City + Ball to start.");
            }
        }));
        workshop.add(tools,BorderLayout.NORTH);workshop.add(board,BorderLayout.CENTER);
        JLabel help=new JLabel("Drag cards together • Or click two cards • Right-click a card to remove it");help.setForeground(MUTED);help.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));workshop.add(help,BorderLayout.SOUTH);
        center.add(workshop,BorderLayout.CENTER);
        JPanel collection=new JPanel(new BorderLayout(0,10));collection.setPreferredSize(new Dimension(287,400));collection.setOpaque(false);
        JPanel filters=new JPanel(new BorderLayout(0,8));filters.setOpaque(false);
        JLabel label=new JLabel("YOUR COLLECTION");label.setForeground(INK);label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,15));filters.add(label,BorderLayout.NORTH);
        search.setBorder(BorderFactory.createTitledBorder("Search discoveries"));
        search.setToolTipText("Search discovered items");search.putClientProperty("JTextField.placeholderText","Search discoveries...");
        search.getAccessibleContext().setAccessibleName("Search discovered items");filters.add(search,BorderLayout.CENTER);filters.add(category,BorderLayout.SOUTH);
        collection.add(filters,BorderLayout.NORTH);
        library.setFixedCellHeight(59);library.setBackground(PAPER);library.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        library.setCellRenderer(new LibraryRenderer());
        JScrollPane scroll=new JScrollPane(library);scroll.setBorder(BorderFactory.createLineBorder(new Color(215,225,217)));collection.add(scroll,BorderLayout.CENTER);
        JLabel note=new JLabel("<html>Click or drag an item onto the board.<br>Every discovery is reusable.</html>");note.setForeground(MUTED);collection.add(note,BorderLayout.SOUTH);
        center.add(collection,BorderLayout.EAST);add(center,BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(13,26,18,26));status.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));status.setForeground(TEAL);add(status,BorderLayout.SOUTH);
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){refresh();}public void removeUpdate(DocumentEvent e){refresh();}public void changedUpdate(DocumentEvent e){refresh();}
        });category.addActionListener(e->refresh());installLibraryDrag();refresh();seedBoard();
        say("Start here: combine City + Ball. Discover sports, cities, and legendary events!");
    }
    private JButton button(String title,Runnable action) {
        JButton b=new JButton(title);b.setFocusPainted(false);b.setForeground(INK);b.addActionListener(e->action.run());return b;
    }
    private void seedBoard(){board.addToken("City",110,150);board.addToken("Ball",350,150);}
    private void refresh(){
        listModel.clear();for(AlchemyModel.Element e:model.library(search.getText(),(String)category.getSelectedItem()))listModel.addElement(e);
        progress.setText(model.discovered.size()+" / "+model.elements.size()+" DISCOVERED");
    }
    private void say(String s){status.setText("<html>"+s+(model.saveWarning.isEmpty()?"":"<br>"+model.saveWarning)+"</html>");}
    private void hint(){
        AlchemyModel.Recipe r=model.hint();
        if(r==null){say("Collection complete! You built a whole world of cities and sports.");return;}
        board.addToken(r.first(),80,90);board.addToken(r.second(),330,90);
        say("Try "+r.first()+" + "+r.second()+". The ingredients are on your board.");
    }
    private void journal(){
        DefaultTableModel rows=new DefaultTableModel(new String[]{"Ingredient 1","Ingredient 2","Discovery"},0){
            private static final long serialVersionUID=1L;
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        for(AlchemyModel.Recipe r:model.recipes)if(model.discovered.contains(r.result()))rows.addRow(new Object[]{r.first(),r.second(),r.result()});
        JTable table=new JTable(rows);table.setRowHeight(30);table.setAutoCreateRowSorter(true);
        JScrollPane scroll=new JScrollPane(table);scroll.setPreferredSize(new Dimension(650,420));
        JOptionPane.showMessageDialog(this,scroll,"Recipe journal / "+rows.getRowCount()+" recipes discovered",JOptionPane.PLAIN_MESSAGE);
    }
    private void installLibraryDrag(){
        MouseAdapter drag=new MouseAdapter(){
            String name;Point origin;
            @Override public void mousePressed(MouseEvent e){
                int i=library.locationToIndex(e.getPoint());
                if(!SwingUtilities.isLeftMouseButton(e)||i<0||!library.getCellBounds(i,i).contains(e.getPoint()))return;
                name=listModel.get(i).name();origin=e.getPoint();
            }
            @Override public void mouseDragged(MouseEvent e){
                if(name==null)return;
                Point p=SwingUtilities.convertPoint(library,e.getPoint(),board);
                board.previewName=name;board.previewPoint=p;board.repaint();
            }
            @Override public void mouseReleased(MouseEvent e){
                if(name==null)return;
                Point p=SwingUtilities.convertPoint(library,e.getPoint(),board);
                if(board.contains(p))board.dropLibrary(name,p.x-Board.CARD_W/2,p.y-Board.CARD_H/2);
                else if(origin.distance(e.getPoint())<6)board.addToken(name,-1,-1);
                name=null;board.previewName=null;board.repaint();
            }
        };library.addMouseListener(drag);library.addMouseMotionListener(drag);
    }
    static Color color(AlchemyModel.Element e){return switch(e.category()){
        case "Cities"->new Color(91,101,178);case "Sports"->TEAL;case "Events"->CORAL;
        case "Culture"->new Color(166,118,53);default->new Color(86,139,157);
    };}
    static void drawCard(Graphics2D g,AlchemyModel.Element e,int x,int y,int w,int h,boolean selected){
        g.setColor(new Color(25,43,61,14));g.fillRoundRect(x+2,y+4,w,h,17,17);
        g.setColor(Color.WHITE);g.fillRoundRect(x,y,w,h,17,17);
        g.setColor(selected?TEAL:new Color(215,226,220));g.setStroke(new BasicStroke(selected?2.5f:1));g.drawRoundRect(x,y,w,h,17,17);
        g.setColor(color(e));g.fillRoundRect(x+10,y+12,36,36,11,11);
        g.setColor(Color.WHITE);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));FontMetrics fm=g.getFontMetrics();g.drawString(e.symbol(),x+28-fm.stringWidth(e.symbol())/2,y+36);
        g.setColor(INK);int font=e.name().length()>17?11:e.name().length()>13?12:14;
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,font));g.drawString(e.name(),x+55,y+27);
        g.setColor(MUTED);g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,10));g.drawString(e.category().toUpperCase(),x+55,y+44);
    }
    private final class LibraryRenderer extends JPanel implements ListCellRenderer<AlchemyModel.Element>{
        private static final long serialVersionUID=1L;
        private transient AlchemyModel.Element element;private boolean chosen;
        @Override public Component getListCellRendererComponent(JList<? extends AlchemyModel.Element> list,AlchemyModel.Element value,int index,boolean selected,boolean focus){element=value;chosen=selected;return this;}
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(PAPER);g.fillRect(0,0,getWidth(),getHeight());drawCard(g,element,4,2,getWidth()-10,52,chosen);g.dispose();
        }
    }
    final class Board extends JPanel {
        private static final long serialVersionUID=1L;
        static final int CARD_W=184,CARD_H=62;
        final transient List<Token> tokens=new ArrayList<>();
        transient Token selected,dragging;
        private Point press;private int offsetX,offsetY;private boolean moved;
        String previewName;Point previewPoint;
        Board(){
            setBackground(new Color(235,240,230));setBorder(BorderFactory.createLineBorder(new Color(215,225,217)));
            MouseAdapter mouse=new MouseAdapter(){
                @Override public void mousePressed(MouseEvent e){
                    Token t=hit(e.getPoint(),null);
                    if(SwingUtilities.isRightMouseButton(e)){if(t!=null){tokens.remove(t);if(selected==t)selected=null;repaint();}return;}
                    if(!SwingUtilities.isLeftMouseButton(e)||t==null){selected=null;repaint();return;}
                    dragging=t;press=e.getPoint();offsetX=e.getX()-t.x;offsetY=e.getY()-t.y;moved=false;
                    tokens.remove(t);tokens.add(t);repaint();
                }
                @Override public void mouseDragged(MouseEvent e){
                    if(dragging==null)return;
                    moved|=press.distance(e.getPoint())>5;
                    dragging.x=clamp(e.getX()-offsetX,0,getWidth()-CARD_W-1);dragging.y=clamp(e.getY()-offsetY,52,getHeight()-CARD_H-1);repaint();
                }
                @Override public void mouseReleased(MouseEvent e){
                    if(dragging==null)return;
                    Token t=dragging;dragging=null;
                    if(moved){Token other=overlap(t);if(other!=null)mix(t,other);}
                    else if(selected!=null && selected!=t)mix(selected,t);
                    else selected=selected==t?null:t;
                    repaint();
                }
            };addMouseListener(mouse);addMouseMotionListener(mouse);
        }
        void addToken(String name,int x,int y){
            if(!model.discovered.contains(name))return;
            if(tokens.size()>=40){say("Board is full. Remove a card or clear the board; your collection stays safe.");return;}
            if(x<0){int slot=tokens.size()%12;x=25+slot%3*200;y=90+slot/3*85;}
            tokens.add(new Token(name,clamp(x,0,Math.max(600,getWidth())-CARD_W-1),clamp(y,52,Math.max(400,getHeight())-CARD_H-1)));repaint();
        }
        void dropLibrary(String name,int x,int y){
            int old=tokens.size();addToken(name,x,y);if(tokens.size()==old)return;
            Token t=tokens.get(tokens.size()-1),other=overlap(t);if(other!=null)mix(t,other);repaint();
        }
        private Token hit(Point p,Token exclude){for(int i=tokens.size()-1;i>=0;i--){Token t=tokens.get(i);if(t!=exclude && new Rectangle(t.x,t.y,CARD_W,CARD_H).contains(p))return t;}return null;}
        private Token overlap(Token t){return hit(new Point(t.x+CARD_W/2,t.y+CARD_H/2),t);}
        void mix(Token a,Token b){
            AlchemyModel.Discovery result=model.combine(a.name,b.name);
            if(result==null){say(a.name+" + "+b.name+" didn't combine. Try another pair, or ask for a hint.");selected=null;return;}
            int x=(a.x+b.x)/2,y=(a.y+b.y)/2;tokens.remove(a);tokens.remove(b);selected=null;addToken(result.result(),x,y);
            refresh();say((result.fresh()?"NEW DISCOVERY!  ":"Rediscovered:  ")+a.name+" + "+b.name+" → "+result.result()+(model.discovered.size()==model.elements.size()?" • COLLECTION COMPLETE!":""));
        }
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(206,219,205));for(int x=16;x<getWidth();x+=24)for(int y=65;y<getHeight();y+=24)g.fillOval(x,y,2,2);
            g.setColor(MUTED);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));g.drawString("THE MIXING FLOOR  /  CITIES + SPORTS + POSSIBILITIES",20,30);
            if(tokens.isEmpty()){g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,22));g.drawString("What will you discover next?",35,140);g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14));g.drawString("Bring an item over from your collection to start.",35,170);}
            for(Token t:tokens)drawCard(g,model.elements.get(t.name),t.x,t.y,CARD_W,CARD_H,t==selected||t==dragging);
            if(previewName!=null && previewPoint!=null && contains(previewPoint)){g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.8f));drawCard(g,model.elements.get(previewName),previewPoint.x-CARD_W/2,previewPoint.y-CARD_H/2,CARD_W,CARD_H,true);}
            g.dispose();
        }
    }
    static final class Token {final String name;int x,y;Token(String name,int x,int y){this.name=name;this.x=x;this.y=y;}}
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(value,Math.max(min,max)));}
}
