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
//      Make the window slightly smaller than Conflicts window.
//
//=========================================================================

package mouse.explorer;

//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI_Explorer
//
//-------------------------------------------------------------------------
//
//  The main Explorer window.
//
//  It has seven paragraphs:
//  (0) One line showing the conflict being explored.
//  (1) Text paragraph containing newline or text 'filter:'.
//  (2) Pragraph of lines shownig conflicting terminal pairs.
//  (3) Text paragraph containing newline.
//  (4) Pragraph of item lines, showing the expression 'e1'.
//  (5) Text paragraph containing '===================='.
//  (6) Pragraph of item lines, showing the expression 'e2 Tail(A)'.
//
//  Only the paragraphs (2), (4) and (6) are selectable.
//
//  This window is a singleton. The static method 'display' either creates
//  a new window or displays the existing one.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class GUI_Explorer extends GUI
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  // Existing instance of this window.
  //-------------------------------------------------------------------
  private static GUI_Explorer instance = null;

  //-------------------------------------------------------------------
  // The conflict beig explored.
  //-------------------------------------------------------------------
  Conflict conflict;                  // The conflict being explored

  //-------------------------------------------------------------------
  //  Copies of paragraphs, kept to preserve their type.
  //  Elements of 'paragraphs' in GUI are of type Paragraph.
  //-------------------------------------------------------------------
  Paragraph_Terms paraT = null;       // Terminal pairs
  Paragraph_Explorer para1 = null;    // Paragraphs for two parts ..
  Paragraph_Explorer para2 = null;    // .. of the conflict

  //-------------------------------------------------------------------
  // Selected paragraph.
  //-------------------------------------------------------------------
  Paragraph_Explorer myPara = null;   // Selected Explorer paragraph
  TermPair myTerms = null;            // Selected TermsPair

  //-------------------------------------------------------------------
  // Terminal pair used as filter.
  //-------------------------------------------------------------------
  TermPair filter = null;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4713L;


  //=====================================================================
  //
  //  Constructor
  //
  //=====================================================================
  private GUI_Explorer(Conflict c)
    {
      super(7,92,30);

      addKey("Expand");
      addKey("Filter");
      addKey("Strip");
      addKey("Delete");
      addKey("Delete Line");
      addKey("Choose Line");
      addKey("Reset");

      initialize(c);
    }

  //-------------------------------------------------------------------
  //  Initialize for conflict 'c'
  //-------------------------------------------------------------------
  private void initialize(final Conflict c)
    {
      conflict = c;
      initParagraphs(c);
      paragraphs[0] = new Paragraph_Text(conflict.asString()+"\n");
      paragraphs[3] = new Paragraph_Text("\n");
      paragraphs[5] = new Paragraph_Text("====================\n");
      write();
      setTitle(conflict.expr.name + ":  " + conflict.asString());
    }

  //-------------------------------------------------------------------
  //  Initialize explorer paragraphs for conflict 'c'
  //-------------------------------------------------------------------
  private void initParagraphs(final Conflict c)
    {
      para1 = new Paragraph_Explorer(conflict.arg1,null);
      para2 = new Paragraph_Explorer(conflict.arg2,conflict.expr);
      paraT = new Paragraph_Terms(conflict);
      paragraphs[1] = new Paragraph_Text("\n");
      paragraphs[2] = paraT;
      paragraphs[4] = para1;
      paragraphs[6] = para2;
    }

  //-------------------------------------------------------------------
  //  Display instance for conflict 'c'.
  //-------------------------------------------------------------------
  static void display(final Conflict c)
    {
      if (instance==null)
        instance = new GUI_Explorer(c);
      else
        instance.initialize(c);
      GUI.display(instance);
    }


  //=====================================================================
  //
  //  Actions
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Mouse action.
  //-------------------------------------------------------------------
  void mouseAction(boolean clicked)
    {
      myPara = null;
      myTerms = null;
      if (selected==null) return;
      if (selected.para==2) { myTerms = paraT.line(selected.line).terms; }
      else if (selected.para==4) { myPara = para1; }
      else if (selected.para==6) { myPara = para2; }
      if (clicked)
      {
        if (myPara!=null) keyPressed("Expand");
        if (myTerms!=null)
        {
          filter = myTerms;
          paraT = new Paragraph_Terms(filter);
          paragraphs[1] = new Paragraph_Text("\n  filter:\n");
          paragraphs[2] = paraT;
          write();
        }
      }
    }

  //-------------------------------------------------------------------
  //  Key 'command' pressed.
  //-------------------------------------------------------------------
  void keyPressed(String command)
    {
      switch(command)
      {
        //-------------------------------------------------------------
        //  Expand
        //-------------------------------------------------------------
        case "Expand":
        {
          if (myPara==null) return;
          myPara.expand(selected.item,selected.line);
          if (filter!=null)
          {
            para1.selectWithFirst(filter.term1);
            para2.selectWithFirst(filter.term2);
          }
          write();
          return;
        }

        //-------------------------------------------------------------
        //  Filter
        //-------------------------------------------------------------
        case "Filter":
        {
          if (filter!=null)
          {
            para1.selectWithFirst(filter.term1);
            para2.selectWithFirst(filter.term2);
          }
          else
          {
            para1.filterWith(para2.terms());
            para2.filterWith(para1.terms());
          }
          write();
          return;
        }

        //-------------------------------------------------------------
        //  Strip
        //-------------------------------------------------------------
        case "Strip":
        {
          if (para1.nLines()==0) return;
          if (para2.nLines()==0) return;
          Line_Items line1 = para1.line(0);
          Line_Items line2 = para2.line(0);
          if (!line1.item(0).asString().equals(line2.item(0).asString())) return;
          para1.strip();
          para2.strip();
          write();
          return;
        }

        //-------------------------------------------------------------
        //  Delete
        //-------------------------------------------------------------
        case "Delete":
        {
          if (myPara==null) return;
          Line_Items line = myPara.line(selected.line);
          line.removeItem(selected.item);
          write();
          return;
        }

        //-------------------------------------------------------------
        //  Delete Line
        //-------------------------------------------------------------
        case "Delete Line":
        {
          if (myPara==null) return;
          myPara.removeLine(selected.line);
          write();
          return;
        }

        //-------------------------------------------------------------
        //  Choose Line
        //-------------------------------------------------------------
        case "Choose Line":
        {
          if (myPara==null) return;
          Line_Items line = myPara.line(selected.line);
          myPara.clear();
          myPara.addLine(line);
          write();
          return;
        }

        //-------------------------------------------------------------
        //  Reset
        //-------------------------------------------------------------
        case "Reset":
        {
          initParagraphs(conflict);
          write();
          return;
        }
        default: ;
      }
    }

  //-------------------------------------------------------------------
  //  Window closed.
  //-------------------------------------------------------------------
  void windowClosed()
    { instance = null; }
}