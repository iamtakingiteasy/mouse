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
//
//=========================================================================

package mouse.explorer;

import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Semantics
//
//-------------------------------------------------------------------------
//
//  Semantics for the parser that parses the grammar being explored.
//  Generates trees of Expr objects, rooted in 'rules'.
//  Builds a list of all Expr.Ref objects in 'refs'.
//  Counts syntaz errors in 'errcount'.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Semantics extends mouse.runtime.SemanticsBase
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Results: list of Rules, list of Refs and number of errors.
  //-------------------------------------------------------------------
  public Vector<Expr> rules = new Vector<Expr>();
  public Vector<Expr.Ref> refs = new Vector<Expr.Ref>();
  public int errcount = 0;


  //=====================================================================
  //
  //  Some shorthands
  //
  //=====================================================================

  Expr exprValue(int i)
    { return (Expr)rhs(i).get(); }

  String stringValue(int i)
    { return (String)rhs(i).get(); }

  char charValue(int i)
    { return (Character)rhs(i).get(); }


  //=======================================================================
  //
  //  Semantic procedures
  //
  //=======================================================================
  //-------------------------------------------------------------------
  //  Grammar = Space (Rule / Skip)*+ EOT
  //              0         1,2,..,-2    -1
  //-------------------------------------------------------------------
  void Grammar()
    {
      int n = rhsSize()-2; // Number of Rules, correct or not.
      if (n<=0)
      {
        System.out.println("input file empty");
        errcount++;
      }
    }

  //-------------------------------------------------------------------
  //  Rule = Name EQUAL RuleRhs DiagName? SEMI
  //           0    1      2        3     3,4
  //-------------------------------------------------------------------
  void Rule()
    {

      Expr r = exprValue(2);   // RuleRhs returns Expr object for the Rule.
      r.name = stringValue(0);
      r.isRule = true;
      rules.add(r);
      lhs().put(r);
    }

  //-------------------------------------------------------------------
  //  failed Rule = Name EQUAL RuleRhs DiagName? SEMI
  //-------------------------------------------------------------------
  void Error()
    {
      System.out.println(lhs().errMsg());
      lhs().errClear();
      errcount++;
    }

  //-------------------------------------------------------------------
  //  RuleRhs = Sequence Actions (SLASH Sequence Actions)*
  //                0       1     2,5,.. 3,6,..   4,7,..
  //-------------------------------------------------------------------
  void RuleRhs()
    {
      int n = (rhsSize()+1)/3;  // Number of 'Sequence's.
      if (n==1)
      {
        lhs().put(exprValue(0));
        return;
      }
      Expr[] seq  = new Expr[n];
      for (int i=0;i<n;i++)
        seq[i] = exprValue(3*i);
      lhs().put(new Expr.Choice(seq));
    }

  //-------------------------------------------------------------------
  //  Choice = Sequence (SLASH Sequence)*
  //               0     1,3,..  2,4,..
  //-------------------------------------------------------------------
  void Choice()
    {
      int n = (rhsSize()+1)/2;  // Number of 'Sequence's.
      if (n==1)
      {
        lhs().put(exprValue(0));
        return;
      }
      Expr[] seq  = new Expr[n];
      for (int i=0;i<n;i++)
        seq[i] = exprValue(2*i);
      lhs().put(new Expr.Choice(seq));
    }

  //-------------------------------------------------------------------
  //  Sequence = Prefixed+
  //               0,1,..
  //-------------------------------------------------------------------
  void Sequence()
    {
      int n = rhsSize();  // Number of 'Prefixed's.
      if (n==1)
      {
        lhs().put(exprValue(0));
        return;
      }
      Expr[] pref = new Expr[n];
      for (int i=0;i<n;i++)
        pref[i] = exprValue(i);
      lhs().put(new Expr.Sequence(pref));
    }

  //-------------------------------------------------------------------
  //  Prefixed = PREFIX? Suffixed
  //                0     1(0)
  //-------------------------------------------------------------------
  void Prefixed()
    {
      // If no prefix: pass 'Suffixed' on.
      if (rhsSize()==1)
      {
        lhs().put(exprValue(0));
        return;
      }

      Expr suf = exprValue(1);
      if (rhs(0).charAt(0)=='&')
        lhs().put(new Expr.And(suf));
      else
      {
        if (suf instanceof Expr.Any) // End of input
          lhs().put(new Expr.End());
        else
          lhs().put(new Expr.Not(suf));
      }
    }

  //-------------------------------------------------------------------
  //  Suffixed = Primary (UNTIL Primary / SUFFIX)?
  //                 0       1      2         1
  //-------------------------------------------------------------------
  void Suffixed()
    {
      if (rhsSize()==1)           // Primary only
        lhs().put(exprValue(0));

      else if (rhsSize()==2)      // Primary SUFFIX
      {
        if (rhs(1).charAt(0)=='?')
          lhs().put(new Expr.Query(exprValue(0)));
        else if (rhs(1).charAt(0)=='*')
          lhs().put(new Expr.Star(exprValue(0)));
        else
          lhs().put(new Expr.Plus(exprValue(0)));
      }

      else                        // Primary UNTIL Primary
      {
        Expr A = exprValue(0);
        Expr B = exprValue(2);
        Expr notAB = new Expr.Sequence(new Expr.Not(B),A);
        if (rhs(1).charAt(0)=='*')
          lhs().put(new Expr.Sequence(new Expr.Star(notAB),B));
        else
          lhs().put(new Expr.Sequence(new Expr.Plus(notAB),B));
      }
    }

  //-------------------------------------------------------------------
  //  Primary = Name
  //             0
  //-------------------------------------------------------------------
  void Resolve()
    {
      Expr.Ref ref = new Expr.Ref(stringValue(0));
      refs.add(ref);
      lhs().put(ref);
    }
  //-------------------------------------------------------------------
  //  Primary = LPAREN Choice RPAREN
  //               0      1      2
  //-------------------------------------------------------------------
  void Pass2()
    { lhs().put(rhs(1).get()); }

  //-------------------------------------------------------------------
  //  Primary = ANY
  //-------------------------------------------------------------------
  void Any()
    { lhs().put(new Expr.Any()); }

  //-------------------------------------------------------------------
  //  Primary = StringLit
  //  Primary = Range
  //  Primary = CharClass
  //  Char = Escape
  //-------------------------------------------------------------------
  void Pass()
    { lhs().put(rhs(0).get()); }

  //-------------------------------------------------------------------
  //  Actions = OnSucc OnFail
  //-------------------------------------------------------------------
  void Actions()
    {}

  //-------------------------------------------------------------------
  //  OnSucc = (LWING AND? Name? RWING)?
  //-------------------------------------------------------------------
  void OnSucc()
    {}

  //-------------------------------------------------------------------
  //  OnFail = (TILDA LWING Name? RWING)?
  //-------------------------------------------------------------------
  void OnFail()
    {}

  //-------------------------------------------------------------------
  //  Name = Letter (Letter / Digit)* Space
  //            0        1 ... -2       -1
  //-------------------------------------------------------------------
  void Name()
    { lhs().put(rhsText(0,rhsSize()-1)); }

  //-------------------------------------------------------------------
  //  DiagName = "<" Char++ ">" Space
  //-------------------------------------------------------------------
  void DiagName()
    {}

  //-------------------------------------------------------------------
  //  StringLit = ["] (!["] Char)+ ["] Space
  //               0    1,2..,-3    -2   -1
  //-------------------------------------------------------------------
  void StringLit()
    {
      StringBuffer sb = new StringBuffer();
      for (int i=1;i<rhsSize()-2;i++)
        sb.append(charValue(i));
      lhs().put(new Expr.StringLit(sb.toString()));
    }

  //-------------------------------------------------------------------
  //  CharClass = ("[" / "^[") (!"]" Char)+ "]" Space
  //                0      0    1,2..,-3    -2   -1
  //-------------------------------------------------------------------
  void CharClass()
    {
      StringBuffer sb = new StringBuffer();
      for (int i=1;i<rhsSize()-2;i++)
        sb.append(charValue(i));
      lhs().put(new Expr.CharClass(sb.toString(),rhs(0).charAt(0)=='^'));
    }

  //-------------------------------------------------------------------
  //  Range = "[" Char "-" Char "]" Space
  //           0    1   2    3   4    5
  //-------------------------------------------------------------------
  void Range()
    {
      char a = (charValue(1));
      char z = (charValue(3));
      lhs().put(new Expr.Range(a,z));
    }

  //-------------------------------------------------------------------
  //  Char = ![\r\n]_
  //-------------------------------------------------------------------
  void Char()
    { lhs().put(rhs(0).charAt(0)); }

  //-------------------------------------------------------------------
  //  Escape = "\\u" HexDigit HexDigit HexDigit HexDigit
  //              0       1       2        3        4
  //-------------------------------------------------------------------
  void Unicode()
    {
        String s = rhsText(1,5);
        lhs().put((char)Integer.parseInt(s,16));
    }

  //-------------------------------------------------------------------
  //  Escape = "\n"
  //             0
  //-------------------------------------------------------------------
  void Newline()
    { lhs().put('\n'); }

  //-------------------------------------------------------------------
  //  Escape = "\r"
  //             0
  //-------------------------------------------------------------------
  void CarRet()
    { lhs().put('\r'); }

  //-------------------------------------------------------------------
  //  Escape = "\t"
  //             0
  //-------------------------------------------------------------------
  void Tab()
    { lhs().put('\t'); }

  //-------------------------------------------------------------------
  //  Escape = "\" _
  //            0  1
  //-------------------------------------------------------------------
  void Escape()
    { lhs().put(rhs(1).charAt(0)); }

  //-------------------------------------------------------------------
  //  Space = ([ \r\n\t] / Comment)*
  //-------------------------------------------------------------------
  void Space()
    {  lhs().errClear(); }

}