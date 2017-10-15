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
//      Define size and position of the window.
//
//=========================================================================

package mouse.explorer;

//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI_Info
//
//-------------------------------------------------------------------------
//
//  A window displaying unstructured text.
//
//  The window is a singleton. The static method 'display' either creates
//  a new window or displays the existing one.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class GUI_Info extends GUI
{
  //=====================================================================
  //  Data
  //=====================================================================
  //-------------------------------------------------------------------
  // Existing instance of this window
  //-------------------------------------------------------------------
  private static GUI_Info instance = null;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4716L;


  //=====================================================================
  //  Constructor
  //=====================================================================
  private GUI_Info()
    {
      super(0,70,100);
    }

  //-------------------------------------------------------------------
  //  Display with 'text' and 'title'.
  //-------------------------------------------------------------------
  public static void display(final String text, final String title)
    {
      if (instance==null)
        instance = new GUI_Info();
      instance.area.setText(text);
      instance.setTitle(title);
      GUI.display(instance);
    }


  //=====================================================================
  // Actions
  //=====================================================================
  //-------------------------------------------------------------------
  //  Mouse selection made.
  //-------------------------------------------------------------------
  void mouseAction(boolean clicked) {}

  //-------------------------------------------------------------------
  //  Key 'command' pressed.
  //-------------------------------------------------------------------
  void keyPressed(String command) {}

  //-------------------------------------------------------------------
  //  Window closed.
  //-------------------------------------------------------------------
  void windowClosed()
    { instance = null; }
}