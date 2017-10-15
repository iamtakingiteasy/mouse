//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2017 by Roman R. Redziejowski (www.romanredz.se).
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//-------------------------------------------------------------------------
//
//  Change log
//    170301 Created.
//    170901 Version 1.9.1
//      Added window size and position parameters to constructor.
//      Size computed using effective screen size without insets.
//
//=========================================================================

package mouse.explorer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI
//
//-------------------------------------------------------------------------
//
//  Base class for Explorer's windows.
//
//  The information being displayed is structured into paragraphs,
//  lines, and items. The paragraphs are represented by Paragraph objects
//  residing in the array 'paragraphs'.
//  The subclass defines the size of this array and fills it with Paragraph
//  objects. Using the 'write' method, it fills display area of the window
//  with text representation of the paragraphs.
//
//  To support 'write', each Paragraph must provide a method 'writeTo'
//  that appends the formatted paragraph text to the image of display area.
//
//  The title is set using the JFrame method 'setTitle'.
//
//  The subclass may define a number of keys (JButtons) placed at the
//  bottom of the window. They are defined with the help of methods
//  'addKey' and 'addGlue'.
//
//  An instance of GUI subclass is displayed by calling 'display'.
//
//  To handle the user's actions, the class implements Mouse, Action,
//  and Window listeners. It converts their invocations into invocations
//  of methods provided by the subclass.
//
//  The Mouse listener is called when the mouse was released or clicked
//  inside 'area'. If this occurred within one of the window elements
//  (line, item), the listener highlights this element and identifies
//  it in 'selected'.  If 'find' cannot identify such element,
//  'selected' is set to null.
//
//  To support 'find', each Paragraph must provide a method 'find'
//  that checks if a given offset within the display belongs to that
//  paragraph or to its contained element.
//
//  The subclass must override these three abstract methods:
//
//  1. mouseAction(boolean clicked).
//     It is called when the mouse was released or clicked within 'area'.
//     The information on where this occurred is supplied in 'selected'.
//
//  2. keyPressed(command).
//     It is called when key with text 'command' was clicked by the mouse.
//
//  3. windowClosed().
//     It is called when the window is being closed.
//
//  The subclass methods invoked by user's actions may change contents
//  of the window by changing the Paragraphs and invoking 'write'.
//  May also set a new title using 'setTitle'.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public abstract class GUI extends JFrame
implements Runnable, MouseListener, ActionListener, WindowListener
{
  //=====================================================================
  //
  //  Data.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Window structure
  //-------------------------------------------------------------------
  JScrollPane pane;      // Main pane, contains 'area' and 'control'
  JTextComponent area;   // Display area
  Box control;           // Contains buttons

  //-------------------------------------------------------------------
  //  Paragraphs
  //-------------------------------------------------------------------
  Paragraph[] paragraphs;

  //-------------------------------------------------------------------
  //  Window element selected by mouse.
  //-------------------------------------------------------------------
  Element selected = null;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4711L;


  //=====================================================================
  //
  //  Constructing the window.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Create a window with 'n' paragraphs, with size of 'scale' percent
  //  of available screen, and positioned at 'pos'.
  //-------------------------------------------------------------------
  protected GUI(int n, int scale, int pos)
    {
      // Get effective screen size.
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension screenSize = toolkit.getScreenSize();
      Insets insets = toolkit.getScreenInsets(getGraphicsConfiguration());
      screenSize.width -= (insets.left + insets.right);
      screenSize.height -= (insets.top + insets.bottom);

      // Set window size
      int width = screenSize.width*scale/100;
      int height = screenSize.height*scale/100;
      setSize(width,height);
      // setSize(600,350);
      setResizable(true);

      // Set window position
      setLocation(pos,pos);
      // setLocation(0,0);

      // Set up the display area
      area = new JTextArea();
      area.setMargin(new Insets(10,10,10,10));
      area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
      area.setEditable(false);
      pane = new JScrollPane(area,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      add(pane,BorderLayout.CENTER);

      // Set up controls
      control = Box.createHorizontalBox();
      add(control,BorderLayout.SOUTH);

      // Listen to mouse
      area.addMouseListener(this);

      // Listen to window
      addWindowListener(this);

      // Create paragraph array
      paragraphs = new Paragraph[n];
  }

  //-------------------------------------------------------------------
  //  Add key with 'name'.
  //-------------------------------------------------------------------
  JButton addKey(String name)
    {
      JButton key = new JButton(name);
      control.add(key);
      key.addActionListener(this);
      return key;
    }

  //-------------------------------------------------------------------
  //  Add 'elastic glue' between keys.
  //-------------------------------------------------------------------
  void addGlue()
    { control.add(Box.createHorizontalGlue()); }

  //-------------------------------------------------------------------
  //  Fill display area with text represented by the Paragraphs.
  //-------------------------------------------------------------------
  void write()
    {
      // Set up an image of display area in the form of StringBuffer.
      StringBuffer display = new StringBuffer();

      // Let Pragraphs fill the image with their representations.
      for (Paragraph para: paragraphs)
        para.writeTo(display);

      // Fill display area with the constructed image
      area.setText(display.toString());

      // Nothing is selected so far,
      selected = null;
    }


  //=====================================================================
  //
  //  Display the window 'gui'.
  //
  //=====================================================================
  public static void display(GUI gui)
    { SwingUtilities.invokeLater(gui); }

  //-------------------------------------------------------------------
  //  A method of interface Runnable.
  //  Activated by 'invokeLater' in 'Display'
  //-------------------------------------------------------------------
  public void run()
    {
      area.requestFocusInWindow();
      setVisible(true);
      toFront();
      validate();      // Make sure GUI is drawn before next step.
   }


  //=====================================================================
  //
  //  Subclass-defined metods for handling user actions.
  //
  //=====================================================================
  abstract void mouseAction(boolean clicked);
  abstract void keyPressed(String command);
  abstract void windowClosed();


  //=====================================================================
  //
  //  Mouse Listener methods.
  //
  //=====================================================================
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mousePressed(MouseEvent e){}

  //-------------------------------------------------------------------
  //  Find the element where mouse was released.
  //  Highlight it and identify in 'selected'.
  //  If mouse was released outside any object set 'selected' to null.
  //-------------------------------------------------------------------
  public void mouseReleased(MouseEvent e)
    {
      selected = find(e);
      mouseAction(false);
      e.consume();
    }

  //-------------------------------------------------------------------
  //  Find the element where mouse was clicked.
  //  Highlight it, identify in 'selected', and call 'mouseAction'.
  //  If mouse was released outside any object set 'selected' to null.
  //  Invoke 'mouseAction'.
  //-------------------------------------------------------------------
  public void mouseClicked(MouseEvent e)
    {
      selected = find(e);
      if (e.getClickCount()!=2)
        mouseAction(false);
      else
        mouseAction(true);
      e.consume();
    }


  //=====================================================================
  //
  //  Action listener method.
  //  Call subclass method 'keyPressed' with key text as 'command'.
  //
  //=====================================================================
  public void actionPerformed(ActionEvent e)
    {
      String command = e.getActionCommand();
      keyPressed(command);
    }


  //=====================================================================
  //
  //  Window Listener methods.
  //
  //=====================================================================
  public void windowOpened(WindowEvent e){}
  public void windowClosed(WindowEvent e){}
  public void windowActivated(WindowEvent e){}
  public void windowDeactivated(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowIconified(WindowEvent e){}
  public void windowClosing(WindowEvent e)
    { windowClosed(); }


  //=====================================================================
  //
  //  Auxiliary method.
  //  Return the window element where mouse was released or clicked
  //  and highlight it.
  //  Return null if the action occurred outside selectable elements.
  //
  //=====================================================================
  private Element find(MouseEvent e)
    {
      // Find of the mouse event offset in display area text.
      int offset = area.viewToModel(new Point(e.getX(),e.getY()));

      // Let each paragraph check if the event applies to its element.
      for (int i=0;i<paragraphs.length;i++)
      {
        Element elem = paragraphs[i].find(offset);

        // If element identified, highlight and return it.
        if (elem!=null)
        {
          elem.para = i;
          area.select(elem.start,elem.end);
          return elem;
        }
      }

      // Return null if mouse event was outside selectable elements.
      return null;
    }

}
