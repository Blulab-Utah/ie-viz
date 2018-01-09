package edu.pitt.dbmi.nlp.noble.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class ColorTools {
	private static int colorIndex = 0;
	public static final Color ChartGrayColor = new Color(77,77,77);
    public static final Color ChartBlueColor = new Color(93,165,218);
    public static final Color ChartOrangeColor = new Color(250,164,58);
    public static final Color ChartGreenColor = new Color(96,189,104);
    public static final Color ChartPinkColor = new Color(241,124,176);
    public static final Color ChartBrownColor = new Color(178,145,47);
    public static final Color ChartPurpleColor = new Color(156,95,181);
    public static final Color ChartYellowColor = new Color(222,207,63);
    public static final Color ChartRedColor = new Color(241,88,84);
    public static final Color ChartDeepBlueColor = new Color(0,155,187);
    public static final Color ChartDeepYellowColor = new Color(255,177,0);
    public static final Color ChartDeepOrangeColor = new Color(237,89,41);
    public static final Color ChartDeepGreenColor = new Color(63,156,53);
    public static final Color ChartTealColor = new Color(53,196,181);
    public static final Color ChartLightTealColor = new Color(145,232,225);

    public static final List<Color> ChartColorList = Arrays.asList(new Color[]
    		{ChartBlueColor,ChartOrangeColor,ChartGreenColor,ChartPurpleColor,ChartRedColor,ChartGrayColor,
    		 ChartPinkColor,ChartBrownColor,ChartYellowColor,ChartDeepBlueColor,ChartDeepGreenColor,
    		 ChartTealColor,ChartDeepYellowColor,ChartLightTealColor,ChartDeepOrangeColor});

    /**
     * get a random chart friendly color.
     * each invokation returns a color and then
     * cycles back
     * @return color object
     */
    public static Color getChartColor(){
    	if(colorIndex >= ChartColorList.size())
    		colorIndex = 0;
    	return ChartColorList.get(colorIndex++);
    }
}
