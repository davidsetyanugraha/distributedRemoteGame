package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Pane extends JPanel {

  private final JPanel gui = new JPanel(new BorderLayout(3, 3));
  private JButton[][] boardSquares = new JButton[22][22];
  private JPanel board;
  private final JLabel message = new JLabel("Ready to play");
  private static final String COLS = "ABCDEFGHIJKLMNOPQRSTUV";
  private String word;
  private int characterNum, turn;
  private int x, y;
  private Client client;
  private int score;
  private static JSONObject json = null;

  public Pane(ClientFrame clientFrame, Client client) {

    this.client = client;

    // set up the main GUI
    gui.setBorder(new EmptyBorder(5, 5, 5, 5));
    JToolBar tools = new JToolBar();
    tools.setFloatable(false);
    gui.add(tools, BorderLayout.PAGE_START);

    tools.addSeparator();
    tools.add(message);

    // String word hold the input
    // because now One Player, One Turn, One letter
    // word = char, only one letter each turn now
    // characterNum used to calculate times this player try to input in his turn, 
    // used for one-letter-one-turn validation
    word = "";
    characterNum = 0;
    turn = 1;

    JButton btnSubmit = new JButton("VOTE");
    btnSubmit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  // send ask vote message to server
    	  // server broadcast vote to all others
    	  // others then run the implementation below
    	  if(characterNum == 0){
    		  JOptionPane.showMessageDialog(null,"You can't ask vote before input!","Error",JOptionPane.PLAIN_MESSAGE);
    	  }
    	  else{
    	  int vote;
    	  vote = JOptionPane.showConfirmDialog(null, "PLEASE GIVE YOUR VOTE");
    	  //vote = 0, agree; otherwise disagree
    	  //then send this vote to server
    	  //if majority agree, call score function
    	  //move to next player
    	  characterNum = 0;
    	  }
      }
    });
    tools.add(btnSubmit);
    
    JButton btnPass = new JButton("PASS");
    btnPass.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
      	  //move to next turn
      	  characterNum = 0;
        }
      });
    tools.add(btnPass);
    


    board = new JPanel() {

      /**
       * Override the preferred size to return the largest it can, in a square shape. Must (must,
       * must) be added to a GridBagLayout as the only component (it uses the parent as a guide to
       * size) with no GridBagConstaint (so it is centered).
       */
      @Override
      public final Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Dimension prefSize = null;
        Component c = getParent();
        if (c == null) {
          prefSize = new Dimension((int) d.getWidth(), (int) d.getHeight());
        } else if (c != null && c.getWidth() > d.getWidth() && c.getHeight() > d.getHeight()) {
          prefSize = c.getSize();
        } else {
          prefSize = d;
        }
        int w = (int) prefSize.getWidth();
        int h = (int) prefSize.getHeight();
        // the smaller of the two sizes
        int s = (w > h ? h : w);
        return new Dimension(s, s);
      }
    };

    RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
    rl.setRoundingPolicy(RelativeLayout.FIRST);
    rl.setFill(true);
    board.setLayout(rl);

    board.setBorder(new CompoundBorder(new EmptyBorder(8, 8, 8, 8), new LineBorder(Color.BLACK)));
    // Set the BG to be ochre
    Color ochre = new Color(204, 119, 34);
    board.setBackground(SystemColor.activeCaption);
    JPanel boardConstrain = new JPanel(new GridBagLayout());
    boardConstrain.setBackground(SystemColor.desktop);
    boardConstrain.add(board);
    gui.add(boardConstrain);


    // create the board squares

    Insets buttonMargin = new Insets(0, 0, 0, 0);

    // X: j[turn], jj, jj; Y: i[turn], ii, ii;
    int[] yList = new int[500];
    int[] xList = new int[500];
    yList[0] = 10;
    xList[0] = 10;

    for (int i = 0; i < boardSquares.length; i++) {
      for (int j = 0; j < boardSquares[i].length; j++) {

        JButton b = new JButton();
        b.setMargin(buttonMargin);

        boardSquares[10][10] = new JButton(String.valueOf("*"));
        b.setText("");
        final int ii = i;
        final int jj = j;

        b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            yList[turn] = ii;
            xList[turn] = jj;

            // call input window
            String input;
            input = JOptionPane.showInputDialog(null, "Enter the Character");

            int x = xList[turn];
            int y = yList[turn];

            //confirmInput set to check if player confirm to input in this button
            int confirmInput = 1;
            confirmInput = JOptionPane.showConfirmDialog(null, "Are You Sure to Input here?");
            if(confirmInput != 0){
            	input = "";
            	characterNum -= 1;
            	turn -= 1;
            }
            //check if other input in this btn before: check if letter exist, check if is different turn;
            else if (!b.getText().isEmpty()){
            	JOptionPane.showMessageDialog(null,"Letter Already Exist, Try Again.","Error",JOptionPane.PLAIN_MESSAGE);
            			//roll back the word of JSON, should save this data
            			//////////////////
            			characterNum -= 1;
                    	turn -= 1;
            	}
            // check if one button one letter
            else if (input.length() > 1) {
              // exception
              JOptionPane.showMessageDialog(null, "Invalid Input, Please Try Again!", "Error",
                  JOptionPane.PLAIN_MESSAGE);
              input = "";
              characterNum -= 1;
          	  turn -= 1;
            }            
            // each turn each word validation
            else if (characterNum >= 1) {
                JOptionPane.showMessageDialog(null, "One Letter One Turn!", "Error",
                    JOptionPane.PLAIN_MESSAGE);
                input = "";
            	turn -= 1;
              }
            // check if any letter around the letter input
            else{
                boolean findLetter = false;
                for (y = yList[turn] - 1; y <= yList[turn] + 1; y++) {
                    if (!boardSquares[x][y].getText().equalsIgnoreCase("")) {
                        findLetter = true;
                     	}
                for (x = xList[turn] - 1; x <= xList[turn] + 1; x++) {
                    if (!boardSquares[x][y].getText().equalsIgnoreCase("")) {
                      findLetter = true;
                    }
                  }
                	}
                if (findLetter == false) {
                  JOptionPane.showMessageDialog(null, "Please Input Letter in Adjacent Positions",
                      "Error", JOptionPane.PLAIN_MESSAGE);
                  input = "";
                  characterNum -= 1;
              	turn -= 1;
                }
            }
            
            int coordX = xList[turn], coordY = yList[turn];
            b.setText(input);
            if ((b.getText() != "") && (!b.getText().isEmpty())) {
              try {
                appendJson(coordX, coordY, input);
              } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
            }
            word += b.getText();
            characterNum += 1;
            turn += 1;
            submit();
          }
        });


        b.setBackground(Color.WHITE);
        boardSquares[j][i] = b;
        
      }
    }


    // fill the board

    RelativeLayout topRL = new RelativeLayout(RelativeLayout.X_AXIS);
    topRL.setRoundingPolicy(RelativeLayout.FIRST);
    topRL.setFill(true);
    JPanel top = new JPanel(topRL);
    top.setOpaque(false);
    board.add(top, new Float(1));

    top.add(new JLabel(""), new Float(1));

    // fill the top row
    for (int ii = 0; ii < 21; ii++) {
      JLabel label = new JLabel(COLS.substring(ii, ii + 1), SwingConstants.CENTER);
      top.add(label, new Float(1));
    }
    // fill the black non-pawn piece row
    for (int ii = 0; ii < 21; ii++) {

      RelativeLayout rowRL = new RelativeLayout(RelativeLayout.X_AXIS);
      rowRL.setRoundingPolicy(RelativeLayout.FIRST);
      rowRL.setFill(true);
      JPanel row = new JPanel(rowRL);
      row.setOpaque(false);
      board.add(row, new Float(1));

      for (int jj = 0; jj < 21; jj++) {
        switch (jj) {
          case 0:
            row.add(new JLabel("" + (22 - (ii + 1)), SwingConstants.CENTER), new Float(1));
          default:
            row.add(boardSquares[jj][ii], new Float(1));
        }
      }
    }
  }

  public final void submit(){
      /** Backend Call */
      // submit data to server
      JSONArray wordInput = null;
      try {
      	//wordInput = the word found in checking input function
        wordInput = json.getJSONArray("word");
      } catch (JSONException e2) {
        // TODO Auto-generated catch block
        e2.printStackTrace();
      }
      // change score mechanism
      System.out.println("added score:" + wordInput.length());
      score = wordInput.length();

      try {
        client.addWord(json.toString());
      } catch (RemoteException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
  }
  public final JComponent getboard() {
    return board;
  }

  public final JComponent getGui() {
    return gui;
  }

  public String getWord() {
    return word;
  }

  public int getY() {
    return y;
  }

  public int getX() {
    return x;
  }

  public void setChar(int x, int y, String ch) {
    boardSquares[x][y].setText(ch);
  }

  public int getcharacterNum() {
    return characterNum;
  }

  public int getScore() {
    return score;
  }

  private static void appendJson(int x, int y, String ch) throws JSONException {
    System.out.println("APPEND JSON: x = " + x + " , y = " + y + " , ch = " + ch);

    if (json == null) {
      json = new JSONObject();
      JSONArray arrWord = new JSONArray();
      JSONObject obj = new JSONObject();
      obj.put("x", x);
      obj.put("y", y);
      obj.put("ch", ch);
      arrWord.put(obj);
      json.put("word", arrWord);
      json.put("score", arrWord.length());
    } else {
      JSONArray arrWord = json.getJSONArray("word");
      JSONObject obj = new JSONObject();
      obj.put("x", x);
      obj.put("y", y);
      obj.put("ch", ch);
      arrWord.put(obj);
      json.put("score", arrWord.length());
    }

    System.out.println("Final JSON = " + json.toString());
  }
}