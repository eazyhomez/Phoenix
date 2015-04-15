package com.eteks.test;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.FurnitureCategory;
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
	
	public HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[2];
	
	public class RoomTestAction extends PluginAction 
	{		

		public Home home = null;
		public Room room = null;
		
		
		// ======================= CLASSES ======================= //
		
		public class Points
		{
			float x;
			float y;
			
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
			
			try
			{				
				//storeAllFurnRects(home);
				//storeAllWallRects(home);				
				//markBoxes = getMarkerBoxes();
				
				long startTime = System.nanoTime();
				
				// ===================================================== //
				
				/*
				Points centerP = new Points(350.0f, 950.0f);
				float radius = 150.0f;
				
				Points startLine = new Points(0.0f, 800.0f);
				Points endLine = new Points(600.0f, 1000.0f);
				
				List<Points> intList = getIntersectionCircleLine(centerP, radius, startLine, endLine);
				*/
				
				// ===================================================== //		
				/*
				Points a = new Points(300.0f, 1000.0f);
				Points b = new Points(600.0f, 1001.0f);
				
				Points startLine = new Points(0.0f, 800.0f);
				Points endLine = new Points(600.0f, 1000.0f);
				
				boolean bOnSameSide = checkPointOnSameSide(a, b, startLine, endLine);
				*/
				
				// ===================================================== //	
								
				Points a = new Points(450.0f, 943.0f);
				
				Points startLine = new Points(0.0f, 800.0f); 
				Points endLine = new Points(20.0f, 820.0f); //new Points(600.0f, 1000.0f);
				
				float tolerance = 0.50f; // 5 mm 
				
				boolean bInBetween = checkPointInBetween(a, startLine, endLine, tolerance);
				
				// ===================================================== //	
				
				long endTime = System.nanoTime();
				
				JOptionPane.showMessageDialog(null, bInBetween + " -> Time : " + (endTime - startTime) + " ns \n");
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
		}
		
		
		public List<Points> getIntersectionCircleLine(Points center, float rad, Points startL, Points endL)
		{
			List<Points> interList = new ArrayList<Points>();
			
			// Equation of Line
			float m = ((endL.y - startL.y) / (endL.x - startL.x));
			float c = startL.y - (m*startL.x);
			
			// (m^2+1)x^2 + 2(mc−mq−p)x + (q^2−r^2+p^2−2cq+c^2) = 0			
			
			float A = (m*m) + 1;
			float B = 2*((m*c) - (m*center.y) - center.x);
			float C = (center.y*center.y) - (rad*rad) + (center.x*center.x) - 2*(c*center.y) + (c*c);
			
			float D = (B*B) - 4*A*C;
			
			if(D == 0)
			{
				float x1 = ((-B) + (float)Math.sqrt(D)) / (2*A);
				float y1 = (m*x1) + c;
				
				Points inter = new Points(x1, y1);
				interList.add(inter);	
				
				//putMarkers(inter, false);
			}
			else if (D > 0)
			{
				float x1 = ((-B) + (float)Math.sqrt(D)) / (2*A);
				float y1 = (m*x1) + c;
				
				Points inter1 = new Points(x1, y1);
				interList.add(inter1);
				
				//putMarkers(inter1, false);
				
				float x2 = ((-B) - (float)Math.sqrt(D)) / (2*A);
				float y2 = (m*x2) + c;
				
				Points inter2 = new Points(x2, y2);
				interList.add(inter2);
				
				//putMarkers(inter2, false);
			}
			
			return interList;
		}
		
		
		// ======================= UTILITY FUNCTIONS ======================= //
		
		public boolean checkPointOnSameSide(Points a, Points b, Points pS1, Points pS2)
		{
			boolean bRet = false;
			
			// ((y1−y2)(ax−x1)+(x2−x1)(ay−y1))((y1−y2)(bx−x1)+(x2−x1)(by−y1)) < 0
			
			float res = ( ((pS1.y - pS2.y)*(a.x - pS1.x)) + ((pS2.x - pS1.x)*(a.y - pS1.y)) )*( ((pS1.y - pS2.y)*(b.x - pS1.x)) + ((pS2.x - pS1.x)*(b.y - pS1.y)) );
			
			if(res < 0)
				bRet = false;
			else
				bRet = true;
			
			return bRet;
		}
		
		public boolean checkPointInBetween(Points test, Points start, Points end, float tolPercent)
		{
			boolean bRet = false;
			
			float distST = calcDistance(start, test);
			float distTE = calcDistance(test, end);
			float distSE = calcDistance(start, end);
			
			float distSEAbs = (float)(Math.abs(distST + distTE - distSE));
					
			if(distSEAbs <= tolPercent)
				bRet = true;
			
			return bRet;			
		}
		
		public float calcDistance(Points p1, Points p2)
		{
			float d = (float) Math.sqrt(((p2.x - p1.x) * (p2.x - p1.x)) + ((p2.y - p1.y)*(p2.y - p1.y)));
			return d;
		}
		
		// ======================= INIT FUNCTIONS ======================= //
		
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
		
		
		// ======================= DEBUG FUNCTIONS ======================= //
		
		public void putMarkers(Points p, boolean bIsRed)
		{
			HomePieceOfFurniture box = null;
			
			if(bIsRed)
				box = markBoxes[0].clone();
			else
				box = markBoxes[1].clone();
			
			box.setX(p.x);
			box.setY(p.y);
			home.addPieceOfFurniture(box);
		}
		
		public HomePieceOfFurniture[] getMarkerBoxes()
		{
			HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[2];
			int count = 0;
			
			List<FurnitureCategory> fCatg = getUserPreferences().getFurnitureCatalog().getCategories();
			
			for(int c = 0; c < fCatg.size(); c++ )
			{
				if(count >= 2)
					break;
				
				List<CatalogPieceOfFurniture> catPOF = fCatg.get(c).getFurniture();

				for(int p = 0; p < catPOF.size(); p++ )
				{
					if(catPOF.get(p).getName().equals("boxred"))
					{
						markBoxes[0] = new HomePieceOfFurniture(catPOF.get(p));
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxgreen"))
					{
						markBoxes[1] = new HomePieceOfFurniture(catPOF.get(p));
						count++;
					}
					
					if(count >= 2)
						break;
				}	
			}
			
			return markBoxes;
		}
	}
	
	@Override
	public PluginAction[] getActions() 
	{
		return new PluginAction [] {new RoomTestAction()}; 
	}
}
