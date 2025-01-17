/*
 * Created on 08.12.2004
 *
 * SVN header information:
 *  $Author: LBST-PF-3\orahn $
 *  $Rev: 2509 $
 *  $Date: 2006-10-06 12:01:50 +0200 (Fr, 06 Okt 2006) $
 *  $Id: ObjectComparator.java 2509 2006-10-06 10:01:50Z LBST-PF-3\orahn $
 */
package org.openjump.core.apitools.comparisonandsorting;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Ole Rahn
 * <br>
 * <br>FH Osnabr&uuml;ck - University of Applied Sciences Osnabr&uuml;ck,
 * <br>Project: PIROL (2005),
 * <br>Subproject: Daten- und Wissensmanagement
 * 
 * @version $Rev: 2509 $
 * modified: [sstein]: 16.Feb.2009 changed logger-entries to comments
 */
public class ObjectComparator {
    //public static PersonalLogger logger = new PersonalLogger(DebugUserIds.OLE);
    
	public static int compare( Object o1, Object o2 ){
		
		Double value1, value2;
		
		value1 = ObjectComparator.getDoubleValue(o1);
		value2 = ObjectComparator.getDoubleValue(o2);
		
		if (Double.isNaN(value1) || Double.isNaN(value2)){
		    //logger.printError("got NAN");
		}
		return value1.compareTo(value2);		
	}

    /**
     * Method to generate a <code>double</code> value out of different number objects.
     * @param o an object
     * @return a double value representing to given object or <code>Double.NAN</code> if it can't be parsed
     */
	public static double getDoubleValue(Object o){
		double value = Double.NaN;
		
		if (o==null){
		    //logger.printMinorError("got NULL value");
		} else {
			if (Integer.class.isInstance(o)){
				value = ((Integer)o).doubleValue();
			} else if (Double.class.isInstance(o)){
				value = ((Double)o).doubleValue();
            } else if (Float.class.isInstance(o)){
                value = ((Float)o).doubleValue();
			} else if (BigDecimal.class.isInstance(o)){
                value = ((BigDecimal)o).doubleValue();
            } else if (BigInteger.class.isInstance(o)){
                value = ((BigInteger)o).doubleValue();
            } else if (Long.class.isInstance(o)){
                value = ((Long)o).doubleValue();
            } else if (Short.class.isInstance(o)){
                value = ((Short)o).doubleValue();
            } else if (Byte.class.isInstance(o)){
                value = ((Byte)o).doubleValue();
            } else if (String.class.isInstance(o)){
                value = Double.parseDouble(o.toString());
            } else {
			    //logger.printError(" can't get double value... - " + o.getClass().getName());
			}
		}
		return value;
	}
}
