/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.io.FormatException;

/**
 * This parser class takes a string representing a logic statement and parses it
 * into objects that can be used for evaluating attributes of features. It's
 * based on expressions specified in the GeoSym Handbook MIL-HDBK-857A.
 * <P>
 * It can parse the expression given per the specification, such as
 * 42|1|bfc|1|0|1, or the strings specified in the symbol tables in the
 * MIL-DTL-89045A, like bfc=81 AND sta<>0and<>1and<>2and<>3and<>5and<>6and<>11.
 * <P>
 * 
 * @author dietrick
 */
public class GeoSymAttExpression {

   private static Logger logger = Logger.getLogger("com.bbn.openmap.layer.vpf.GeoSymAttExpr");

   public final static int NO_OP = 0;
   public final static int EQUALS_OP = 1;
   public final static int NOT_EQUALS_OP = 2;
   public final static int LESS_THAN_OP = 3;
   public final static int GREATER_THAN_OP = 4;
   public final static int LT_EQUALS_OP = 5;
   public final static int GT_EQUALS_OP = 6;

   public final static int NONE_CONN = 0;
   public final static int or_CONN = 1; // same attribute can be this or that
   public final static int AND_CONN = 2; // different attributes must all be
                                         // this and this
   public final static int and_CONN = 3; // same attribute must be this and that
   public final static int OR_CONN = 4; // one attribute can be this or a
                                        // different attribute can be that

   protected VPFAutoFeatureGraphicWarehouse warehouse;
   protected Expression exp;
   /**
    * The NOOP goes first to preserve the index value of each operator, as
    * specifed in the GeoSym spec.
    */
   protected static String[] ops = new String[] {
      "",
      "=",
      "<>",
      "<",
      ">",
      "<=",
      ">="
   };

   /**
    * Create the expression object given a text representation of it.
    * 
    * @param source
    * @param warehouse used to resolve the ECDIS variables.
    */
   public GeoSymAttExpression(String source, VPFAutoFeatureGraphicWarehouse warehouse) {
      // Warehouse must be set first.
      this.warehouse = warehouse;
      exp = findExpression(source);

      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Parsing: " + source);
         logger.finer(this.toString());
      }
   }

   protected Connector findOp(String source) {
      int ANDIndex = source.lastIndexOf("AND");
      int ORIndex = source.lastIndexOf("OR");

      if (ANDIndex == ORIndex) {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("connector not found in " + source);
         }
         // both -1;
         return null;
      }

      if (ANDIndex > ORIndex) {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("found AND in " + source);
         }
         return new Connector(AND_CONN, ANDIndex);
      } else {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("found OR in " + source);
         }
         return new Connector(OR_CONN, ORIndex);
      }
   }

   public String toString() {
      if (exp != null) {
         return exp.toString();
      } else {
         return "No Expression Defined";
      }
   }

   protected Connector findMiniOp(String source) {
      int ANDIndex = source.lastIndexOf("and");
      int ORIndex = source.lastIndexOf("or");

      if (ANDIndex == ORIndex) {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("connector not found in " + source);
         }
         // both -1;
         return null;
      }

      if (ANDIndex > ORIndex) {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("found and in " + source);
         }
         return new Connector(and_CONN, ANDIndex);
      } else {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("found or in " + source);
         }
         return new Connector(or_CONN, ORIndex);
      }
   }

   protected Expression findMathOp(String source) {
      int opIndex = 1, locIndex = -1;
      Expression exp = null;

      // Need to make sure that the finding one op doesn't obscure another,
      // i.e. finding = but missing <=.

      while (opIndex < 7) {
         locIndex = source.indexOf(ops[opIndex]);

         if (locIndex >= 0) {
            if (opIndex == 1 || opIndex == 3 || opIndex == 4) {
               if (source.contains("<=") || source.contains(">=")) {
                  opIndex++;
                  continue;
               } else {
                  break;
               }
            } else {
               break;
            }
         }
         opIndex++;
      }

      if (locIndex != -1) {
         // Check out right side. If string, then create CompareExpression. If
         // number, ValueExpression
         String rightSide = source.substring(locIndex + ops[opIndex].length());

         String leftSide = null;
         if (locIndex > 0) {
            leftSide = source.substring(0, locIndex);
         }

         if (logger.isLoggable(Level.FINER)) {
            logger.finer("got left side: " + leftSide + " op: " + ops[opIndex] + " and right side: " + rightSide);
         }

         /**
          * So here, We need to make a determination of whether the the left
          * side is a column name from the data, as specified in the FCI, or if
          * it's a ECDIS check. If the right side is a numerical value, we're
          * just looking to test attribute data against hard numbers. We're
          * going to push this decision into the ValueExpression, let it figure
          * out what it should do.
          */
         try {

            Double val = Double.parseDouble(rightSide);

            /**
             * We need to check the length of this String to see if it's 4,
             * which means it's an ECDIS variable, set by the user. On the left
             * side it's just a straight number value comparison that will be
             * provided for the right side.
             */
            if (leftSide != null && leftSide.length() == 4) {
               exp = new ECDISExpression(leftSide, val, opIndex, warehouse);
            } else {
               exp = new ValueExpression(leftSide, val, opIndex);
            }

         } catch (NumberFormatException nfe) {

            /**
             * This expression gets set up here for when a table value is
             * compared against an ECDIS value.
             * 
             * Turns out, there's never a need for the ColumnExpression because
             * any time right side is text, it's actually referring to the value
             * of the ECDIS External Attribute Name, which can be looked up and
             * set as a variable.
             * 
             * exp = new ColumnExpression(leftSide, rightSide, opIndex);
             */

            // TODO Need to handle UNK and NULL!

            double val = warehouse.getExternalAttribute(rightSide);
            if (val < 0) {
               // try to handle some string arguments
               if (rightSide.equalsIgnoreCase("NULL")) {
                  exp = new StringExpression(leftSide, null, opIndex);
               } else {
                  exp = new StringExpression(leftSide, rightSide, opIndex);
               }

            } else {

               exp = new ValueExpression(leftSide, val, opIndex);
            }
         }

      }

      return exp;
   }

   /**
    * Recursive parsing statement. Keys on Connectors (AND, OR) and builds
    * Expressions based on those. Then looks for mini connectors (and, or) and
    * builds on those. Of course, there might just be one expression here, one
    * that is separated by an operator.
    * 
    * @param source
    * @return Expression tree
    */
   protected Expression findExpression(String source) {

      if (source != null && source.length() > 0) {
         source = source.trim();
         if (source.length() == 0) {
            return null;
         }

         String leftSide = source;
         String rightSide = null;

         Connector op = findOp(leftSide);

         if (op != null) {
            rightSide = op.getRightSide(leftSide);
            leftSide = leftSide.substring(0, op.sourceLoc);

            Expression leftExpression = findExpression(leftSide);
            Expression rightExpression = findExpression(rightSide);

            if (leftExpression != null) {
               op.addExpr(leftExpression);
            }
            if (rightExpression != null) {
               op.addExpr(rightExpression);
            }

            return op;
         }

         // Look for mini ops

         op = findMiniOp(leftSide);

         if (op != null) {
            rightSide = op.getRightSide(leftSide);
            leftSide = leftSide.substring(0, op.sourceLoc);

            Expression leftExpression = findExpression(leftSide);
            Expression rightExpression = findExpression(rightSide);

            if (leftExpression != null) {
               op.addExpr(leftExpression);
            }
            if (rightExpression != null) {
               op.addExpr(rightExpression);
            }

            return op;
         }

         // OK, here we are with the base expressions...
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("need to break up: " + source);
         }

         return findMathOp(source);

      }

      return null;
   }

   /**
    * Does the feature in row of fci pass the conditions of this expression.
    * 
    * @param fci
    * @param row
    * @return true if row contents passes evaluation
    */
   public boolean evaluate(FeatureClassInfo fci, int row) {
      boolean ret = true;
      StringBuffer reasoning = null;
      if (logger.isLoggable(Level.FINE)) {
         reasoning = new StringBuffer();
      }

      if (exp != null) {
         ret = exp.evaluate(fci, row, reasoning);
      }
      if (reasoning != null) {
         reasoning.append("\n--------");
         logger.fine(reasoning.toString());
      }
      return ret;
   }

   /**
    * This one is used by the CoverageTable. Does the feature in row of fci pass
    * the conditions of this expression.
    * 
    * @param fci
    * @param row
    * @return true if row passes evaluation
    */
   public boolean evaluate(FeatureClassInfo fci, List<Object> row) {
      boolean ret = true;
      StringBuffer reasoning = null;
      if (logger.isLoggable(Level.FINE)) {
         reasoning = new StringBuffer();
         logger.fine(toString());
      }
      if (exp != null) {
         ret = exp.evaluate(fci, row, reasoning);
      }
      if (reasoning != null) {
         reasoning.append("\n--------");
         logger.fine(reasoning.toString());
      }

      return ret;
   }

   /**
    * Connector class is the part of the expression that contains the logic
    * operation, AND, OR, and and or.
    * 
    * @author dietrick
    */
   public static class Connector
         implements Expression {
      List<Expression> exp;
      int op;
      int sourceLoc;

      public Connector(int op, int sLoc) {
         this.op = op;
         this.sourceLoc = sLoc;
      }

      public void addExpr(Expression expr) {
         if (exp == null) {
            exp = new LinkedList<Expression>();
         }

         if (expr != null) {
            exp.add(expr);
            updateColumnNamesIfNeeded();
         }
      }

      protected void updateColumnNamesIfNeeded() {
         String colName = null;

         for (Expression e : exp) {
            if (e instanceof CompareExpression) {
               String cName = ((CompareExpression) e).colName;
               if (cName != null) {
                  colName = cName;
                  break;
               }
            }
         }

         if (colName != null) {
            for (Expression e : exp) {
               if (e instanceof CompareExpression) {
                  if (((CompareExpression) e).colName == null) {
                     ((CompareExpression) e).colName = colName;
                     break;
                  }
               }
            }
         }
      }

      public String getRightSide(String source) {
         switch (op) {
            case NONE_CONN:
               break;
            case and_CONN:
            case AND_CONN:
               return source.substring(sourceLoc + 3).trim();
            case or_CONN:
            case OR_CONN:
               return source.substring(sourceLoc + 2).trim();
            default:
         }

         return null;
      }

      public boolean evaluate(FeatureClassInfo fci, int row, StringBuffer reasoning) {
         boolean ret = false;
         switch (op) {
            case NONE_CONN:
               break;
            case or_CONN:
               break;
            case AND_CONN:
               ret = true;
               for (Expression e : exp) {
                  ret = e.evaluate(fci, row, reasoning);
                  if (!ret) {
                     break;
                  }
               }
               break;
            case and_CONN:
               break;
            case OR_CONN:
               for (Expression e : exp) {
                  ret = ret || e.evaluate(fci, row, reasoning);
                  if (ret) {
                     break;
                  }
               }
               break;
            default:
         }

         if (reasoning != null) {
            reasoning.append("\n-> " + toString() + ": evaluates " + ret);
         }
         return ret;
      }

      public boolean evaluate(FeatureClassInfo fci, List<Object> row, StringBuffer reasoning) {
         boolean ret = false;
         switch (op) {
            case NONE_CONN:
               break;
            case AND_CONN:
            case and_CONN:
               ret = true;
               for (Expression e : exp) {
                  ret = e.evaluate(fci, row, reasoning);
                  if (!ret) {
                     break;
                  }
               }
               break;
            case or_CONN:
            case OR_CONN:
               for (Expression e : exp) {
                  ret = e.evaluate(fci, row, reasoning);
                  if (ret) {
                     break;
                  }
               }
               break;
            default:
         }

         if (reasoning != null) {
            reasoning.append("\n-> " + toString() + ": evaluates " + ret);
         }
         return ret;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer("Connector[");
         boolean addConn = false;

         String conn = " AND ";
         if (op == OR_CONN) {
            conn = " OR ";
         }

         for (Expression e : exp) {
            if (addConn) {
               sb.append(conn);
            }
            sb.append(e.toString());
            addConn = true;
         }
         sb.append("]");
         return sb.toString();
      }
   }

   /**
    * The ECDISExpression checks the warehouse for user set values when
    * evaluating.
    * 
    * @author dietrick
    */
   public static class StringExpression
         extends CompareExpression {

      protected String val;

      public StringExpression(String colName, String val, int op) {
         super(colName, op);
         if (val == null) {
            val = "";
         }
         this.val = val;
      }

      /**
       *
       */
      public boolean evaluate(FeatureClassInfo fci, int row, StringBuffer reasoning) {
         // Pre-cache column index so we don't have to do lookup for each entry.
         if (colIndex == -1 || this.fci != fci) {
            setIndexes(fci);
         }

         List<Object> fcirow = new ArrayList<Object>();
         try {
            if (fci.getRow(fcirow, row)) {

               if (colIndex < 0) {
                  if (reasoning != null) {
                     reasoning.append("\n  col ").append(colName).append(" not found in FCI[").append(fci.columnNameString()).append("]");
                  }
                  logger.info("col " + colName + " not found in FCI[" + fci.columnNameString() + "]");
                  return false;
               }

               String realVal = fcirow.get(colIndex).toString().trim();
               return test(realVal, val, reasoning);
            } else {
               if (reasoning != null) {
                  reasoning.append("\n  Can't read row ").append(row);
               }
            }
         } catch (FormatException fe) {
            if (reasoning != null) {
               reasoning.append("\n  FormatException reading row ").append(row);
            }
         }
         return false;
      }

      /**
       * For ECDISExpressions, none of the arguments matter.
       */
      public boolean evaluate(FeatureClassInfo fci, List<Object> row, StringBuffer reasoning) {
         // Pre-cache column index so we don't have to do lookup for each entry.
         if (colIndex == -1 || this.fci != fci) {
            setIndexes(fci);
         }

         // The columns aren't found
         if (colIndex == -1) {
            logger.finer("col " + colName + " not found in FCI[" + fci.columnNameString() + "]");
            return false;
         }

         Object realVal = row.get(colIndex);
         if (realVal == null) {
            realVal = "";
         }
         return test(realVal.toString().trim(), val, reasoning);
      }

      /**
       * The basic test for the operator, returning val1 op val2.
       * 
       * @param val1 NOT NULL
       * @param val2 NOT NULL
       * @param buf
       * @return true if operation passes
       */
      protected boolean test(String val1, String val2, StringBuffer buf) {
         boolean ret = false;
         switch (op) {
            case 1:
               ret = val1.equals(val2);
               break;
            case 2:
               ret = !val1.equals(val2);
               break;

         }

         if (buf != null) {
            String operation = null;
            switch (op) {
               case 1:
                  operation = (ret + "=" + val1 + "==" + val2);
                  break;
               case 2:
                  operation = (ret + "=" + val1 + "!=" + val2);
                  break;
            }

            buf.append("\n   " + toString() + ":" + operation);
         }

         return ret;
      }

      public String toString() {
         return "StringExpression[" + colName + " " + ops[op] + " " + val + "]";
      }

   }

   /**
    * The ECDISExpression checks the warehouse for user set values when
    * evaluating.
    * 
    * @author dietrick
    */
   public static class ECDISExpression
         extends ValueExpression {

      VPFAutoFeatureGraphicWarehouse warehouse = null;

      public ECDISExpression(String colName, double val, int op, VPFAutoFeatureGraphicWarehouse warehouse) {
         super(colName, val, op);
         this.warehouse = warehouse;
      }

      /**
       * For ECDISExpressions, none of the arguments matter.
       */
      public boolean evaluate(FeatureClassInfo fci, int row, StringBuffer reasoning) {
         return evaluate(reasoning);
      }

      /**
       * For ECDISExpressions, none of the arguments matter.
       */
      public boolean evaluate(FeatureClassInfo fci, List<Object> row, StringBuffer reasoning) {
         return evaluate(reasoning);
      }

      public boolean evaluate(StringBuffer reasoning) {
         double realVal = warehouse.getExternalAttribute(colName);
         return test(realVal, val, reasoning);
      }

      public String toString() {
         return "ECDISExpression[" + colName + " " + ops[op] + " " + val + "]";
      }

   }

   /**
    * The ValueExpression is a comparison of a FCI value to a numerical value.
    * 
    * @author dietrick
    */
   public static class ValueExpression
         extends CompareExpression {

      double val;

      public ValueExpression(String colName, double val, int op) {
         super(colName, op);
         this.val = val;
      }

      public boolean evaluate(FeatureClassInfo fci, int row, StringBuffer reasoning) {

         // Pre-cache column index so we don't have to do lookup for each entry.
         if (colIndex == -1 || this.fci != fci) {
            setIndexes(fci);
         }

         List<Object> fcirow = new ArrayList<Object>();
         try {
            if (fci.getRow(fcirow, row)) {

               if (colIndex < 0) {
                  if (reasoning != null) {
                     reasoning.append("\n  col ").append(colName).append(" not found in FCI[").append(fci.columnNameString()).append("]");
                  }

                  return false;
               }

               Double realVal = Double.parseDouble(fcirow.get(colIndex).toString());
               return test(realVal, val, reasoning);
            } else {
               if (reasoning != null) {
                  reasoning.append("\n  Can't read row ").append(row);
               }
            }
         } catch (FormatException fe) {
            if (reasoning != null) {
               reasoning.append("\n  FormatException reading row ").append(row);
            }
         } catch (NumberFormatException nfe) {
            if (reasoning != null) {
               reasoning.append("\n  NumberFormatException reading ").append(fcirow.get(colIndex));
            }
         }

         return false;
      }

      public boolean evaluate(FeatureClassInfo fci, List<Object> row, StringBuffer reasoning) {

         // Pre-cache column index so we don't have to do lookup for each entry.
         if (colIndex == -1 || this.fci != fci) {
            setIndexes(fci);
         }

         try {

            if (colIndex < 0) {
               if (reasoning != null) {
                  reasoning.append("\n  col ").append(colName).append(" not found in FCI[").append(fci.columnNameString()).append("]");
               }
               return false;
            }

            Double realVal = Double.parseDouble(row.get(colIndex).toString());
            return test(realVal, val, reasoning);
         } catch (NumberFormatException nfe) {
            if (reasoning != null) {
               reasoning.append("\n  NumberFormatException reading ").append(row.get(colIndex));
            }
         }

         return false;
      }

      public String toString() {
         return "ValueExpression[" + colName + " " + ops[op] + " " + val + "]";
      }
   }

   /**
    * A ColumnExpression is the comparison of an FCI column value against
    * another column value.
    * 
    * @author dietrick
    */
   public static class ColumnExpression
         extends CompareExpression
         implements Expression {

      protected String otherColName;
      protected int otherColIndex = -1;

      public ColumnExpression(String colName, String otherName, int op) {
         super(colName, op);
         this.otherColName = otherName;
      }

      protected void setIndexes(FeatureClassInfo fci) {
         this.fci = fci;
         int columnCount = fci.getColumnCount();
         colIndex = -1;
         otherColIndex = -1;

         for (int column = 0; column < columnCount; column++) {
            if (fci.getColumnName(column).equalsIgnoreCase(colName)) {
               colIndex = column;
            }
            if (fci.getColumnName(column).equalsIgnoreCase(otherColName)) {
               otherColIndex = column;
            }
         }
      }

      public boolean evaluate(FeatureClassInfo fci, int row, StringBuffer reasoning) {

         // Pre-cache column index so we don't have to do lookup for each entry.
         if (colIndex == -1 || otherColIndex == -1 || this.fci != fci) {
            setIndexes(fci);
         }

         // The columns aren't found
         if (colIndex == -1 || otherColIndex == -1) {
            logger.finer("col " + colName + " or " + otherColName + " not found in FCI[" + fci.columnNameString() + "]");
            return false;
         }

         List<Object> fcirow = new ArrayList<Object>();
         try {
            if (fci.getRow(fcirow, row)) {
               Double realVal1 = Double.parseDouble(fcirow.get(colIndex).toString());
               Double realVal2 = Double.parseDouble(fcirow.get(otherColIndex).toString());
               return test(realVal1, realVal2, reasoning);
            }
         } catch (FormatException fe) {
         } catch (NumberFormatException nfe) {
         }

         return false;
      }

      public boolean evaluate(FeatureClassInfo fci, List<Object> row, StringBuffer reasoning) {

         // Pre-cache column index so we don't have to do lookup for each entry.
         if (colIndex == -1 || otherColIndex == -1 || this.fci != fci) {
            setIndexes(fci);
         }

         // The columns aren't found
         if (colIndex == -1 || otherColIndex == -1) {
            logger.finer("col " + colName + " or " + otherColName + " not found in FCI[" + fci.columnNameString() + "]");
            return false;
         }

         try {

            Double realVal1 = Double.parseDouble(row.get(colIndex).toString());
            Double realVal2 = Double.parseDouble(row.get(otherColIndex).toString());
            return test(realVal1, realVal2, reasoning);

         } catch (NumberFormatException nfe) {
         }

         return false;
      }

      public String toString() {
         return "ValueExpression[" + colName + " " + ops[op] + " " + otherColName + "]";
      }
   }

   public static abstract class CompareExpression
         implements Expression {
      protected int op;
      protected FeatureClassInfo fci = null;
      protected String colName;
      protected int colIndex = -1;

      protected CompareExpression(String colName, int op) {
         this.colName = colName;
         this.op = op;
      }

      protected void setIndexes(FeatureClassInfo fci) {
         this.fci = fci;
         colIndex = -1;
         int columnCount = fci.getColumnCount();
         for (int column = 0; column < columnCount; column++) {
            if (fci.getColumnName(column).equalsIgnoreCase(colName)) {
               colIndex = column;
               break;
            }
         }
      }

      /**
       * The basic test for the operator, returning val1 op val2.
       * 
       * @param val1
       * @param val2
       * @param buf
       * @return true of operation passes.
       */
      protected boolean test(double val1, double val2, StringBuffer buf) {
         boolean ret = false;
         switch (op) {
            case 1:
               ret = val1 == val2;
               break;
            case 2:
               ret = val1 != val2;
               break;
            case 3:
               ret = val1 < val2;
               break;
            case 4:
               ret = val1 > val2;
               break;
            case 5:
               ret = val1 <= val2;
               break;
            case 6:
               ret = val1 >= val2;
         }

         if (buf != null) {
            String operation = null;
            switch (op) {
               case 1:
                  operation = (ret + "=" + val1 + "==" + val2);
                  break;
               case 2:
                  operation = (ret + "=" + val1 + "!=" + val2);
                  break;
               case 3:
                  operation = (ret + "=" + val1 + "<" + val2);
                  break;
               case 4:
                  operation = (ret + "=" + val1 + ">" + val2);
                  break;
               case 5:
                  operation = (ret + "=" + val1 + "<=" + val2);
                  break;
               case 6:
                  operation = (ret + "=" + val1 + ">=" + val2);
            }

            buf.append("\n   " + toString() + ":" + operation);
         }

         return ret;
      }

   }

   /**
    * The Expression interface allows for the recursive queries of Connectors
    * and Value/CompareExpressions.
    * 
    * @author dietrick
    */
   public interface Expression {

      public boolean evaluate(FeatureClassInfo fci, int row, StringBuffer reasoning);

      public boolean evaluate(FeatureClassInfo fci, List<Object> row, StringBuffer reasoning);

   }

   public static void main(String[] args) {
      new GeoSymAttExpression("mac=2 AND idsm=0 AND hdp>=msscand<ssdc AND isdm=0", new VPFAutoFeatureGraphicWarehouse());
   }
}
