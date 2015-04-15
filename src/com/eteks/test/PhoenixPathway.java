package com.eteks.test;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

public class PhoenixPathway extends Plugin 
{
	
	public List<String> furnIds = new ArrayList<String>();
	public List<float[][]> furnRects = new ArrayList<float[][]>();
	public List<Float> furnThicks = new ArrayList<Float>();
	
	public List<float[][]> furnRectsBloated = new ArrayList<float[][]>();
	
	//public List<HomePieceOfFurniture> furnList = new ArrayList<HomePieceOfFurniture>();
	
	public class RoomTestAction extends PluginAction 
	{		

		public Home home = null;
		public Room room = null;
		
		
		// ======================= CLASSES ======================= //
		
		public class Points
		{
			float x;
			float y;
			
			public Points()
			{
				x = -10.0f;
				y = -10.0f;
			}
			
			public Points(float xCoord , float yCoord)
			{
				x = xCoord;
				y = yCoord;
			}
		}				
		
		public class LineSegement
		{
			Points startP;		// x, y
			Points endP;		// x, y
			
			public LineSegement(Points sP, Points eP)
			{
				startP = sP;
				endP = eP;
			}
		}	
		
		public class WallSegement
		{
			Points startP;		// x, y
			Points endP;		// x, y
			float len;
			
			public WallSegement(Points sP, Points eP, float l)
			{
				startP = sP;
				endP = eP;
				len = l;
			}
		}
		
		public class Intersect
		{
			Points p;
			float dist;
			
			public Intersect(Points inP, float inD)
			{
				p = inP;
				dist = inD;
			}
		}
		
		public class InterPoints
		{
			Points p;
			boolean bOrg;
			
			public InterPoints(Points inP, boolean inB)
			{
				p = inP;
				bOrg = inB;
			}
		}
		
		public RoomTestAction() 
		{
			putPropertyValue(Property.NAME, "PhoenixPathway");
			putPropertyValue(Property.MENU, "Phoenix-Fresh");

			// Enables the action by default
			setEnabled(true);
		}	
		
		@Override
		public void execute() 
		{	
			home = getHome();
			room = home.getRooms().get(0);
			
			long startTime = System.currentTimeMillis();
			
			try
			{				
				storeAllFurnRects(home);
				storeAllWallRects(home);

				
				long endTime = System.currentTimeMillis();
				
				JOptionPane.showMessageDialog(null, "Time : " + (endTime - startTime) + " ms \n");
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
		}
		
		public void storeAllFurnRects(Home h)
		{			
			for(HomePieceOfFurniture hp: h.getFurniture())
			{
				String fName = hp.getName();
				
				if(!fName.equals("boxred") && !fName.equals("boxgreen") )
				{
					//furnList.add(hp);
					
					furnIds.add(fName);
					furnRects.add(hp.getPoints());
					furnThicks.add(0.0f);
					/*
					HomePieceOfFurniture hClone = hp.clone();
					float d = hp.getDepth();
					float w = hp.getWidth();
					
					hClone.setDepth(d + FURNITURE_BLOAT_SIZE);
					hClone.setWidth(w + FURNITURE_BLOAT_SIZE);
					hClone.setElevation(0.0f);
					
					furnRectsBloated.add(hClone.getPoints());
					*/
				}
			}
		}
		
		
		public void storeAllWallRects(Home h)
		{
			int wallCount = 1;
			
			//String debugStr = "";
			
			for(Wall w: h.getWalls())
			{
				furnIds.add("wall_" + wallCount);
				
				float[][] wRect = w.getPoints();
				furnRects.add(wRect);
				furnThicks.add(w.getThickness());		
				
				//debugStr += ("Wall_"+ wallCount +" : " + wRect[0][0] + "," + wRect[0][1] + " / " + wRect[1][0] + "," + wRect[1][1] + " / " + wRect[2][0] + "," + wRect[2][1] + " / " + wRect[3][0] + "," + wRect[3][1] + "\n\n");
				
				wallCount++;
			}
			
			//JOptionPane.showMessageDialog(null, furnRect);
		}
	}
	
	@Override
	public PluginAction[] getActions() 
	{
		return new PluginAction [] {new RoomTestAction()}; 
	}
}
