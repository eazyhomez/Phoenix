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
		
		public float ROOM_TOLERANCE = 0.51f;
		
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
			room = home.getRooms().get(0);
			
			try
			{				
				storeAllFurnRects(home);
				storeAllWallRects(home);				
				markBoxes = getMarkerBoxes();
				
				long startTime = System.nanoTime();
				
				// ===================================================== //
				/*			
				Points centerP = new Points(200.0f, 400.0f);
				float radius = 141.4f;
				
				Points startLine = new Points(-100.0f, 300.0f);
				Points endLine = new Points(500.0f, 300.0f);
				
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
				/*				
				Points a = new Points(450.0f, 943.0f);
				
				Points startLine = new Points(0.0f, 800.0f); 
				Points endLine = new Points(20.0f, 820.0f); //new Points(600.0f, 1000.0f);
				
				float tolerance = 0.50f; // 5 mm 
				
				boolean bInBetween = checkPointInBetween(a, startLine, endLine, tolerance);
				*/
				
				// ===================================================== //	
				/*
				String debugStr = "";
				
				double MAX_ANGLE = (180 * (float)(Math.PI/180));
				double ANGLE_ADJUSTMENT = -(20 * (float)(Math.PI/180));
				
				Points centerP = new Points(100.0f, 300.0f);
				float radius = 141.4f;
				
				Points startLine = new Points(-100.0f, 100.0f);
				Points endLine = new Points(300.0f, 500.0f);			

				float aX1 = centerP.x + (radius * (float)(Math.cos(ANGLE_ADJUSTMENT)));
				float aY1 = centerP.y + (radius * (float)(Math.sin(ANGLE_ADJUSTMENT)));
				Points pArc1 = new Points(aX1, aY1);
				
				float aX2 = centerP.x + (radius * (float)(Math.cos(MAX_ANGLE - ANGLE_ADJUSTMENT)));
				float aY2 = centerP.y + (radius * (float)(Math.sin(MAX_ANGLE - ANGLE_ADJUSTMENT)));
				Points pArc2 = new Points(aX2, aY2);
				
				List<Points> interP = getIntersectionArcLineSeg(centerP, radius, startLine, endLine, pArc1, pArc2);
				*/
				
				// ===================================================== //	
				/*
				double MAX_ANGLE = (180 * (float)(Math.PI/180));
				double ANGLE_ADJUSTMENT = -(20 * (float)(Math.PI/180));
				
				Points centerP = new Points(200.0f, 400.0f);
				float radius = 141.4f;
				
				List<LineSegement> lsList = new ArrayList<LineSegement>();
				Points minInter = null;
				
				Points startLine1 = new Points(0.0f, 300.0f);
				Points endLine1 = new Points(400.0f, 300.0f);
				LineSegement ls1 = new LineSegement(startLine1, endLine1);
				lsList.add(ls1);
				
				Points startLine2 = new Points(400.0f, 300.0f);
				Points endLine2 = new Points(420.0f, 320.0f);
				LineSegement ls2 = new LineSegement(startLine2, endLine2);
				lsList.add(ls2);
				
				Points startLine3 = new Points(420.0f, 320.0f);
				Points endLine3 = new Points(-20.0f, 320.0f);
				LineSegement ls3 = new LineSegement(startLine3, endLine3);
				lsList.add(ls3);
				
				Points startLine4 = new Points(-20.0f, 320.0f);
				Points endLine4 = new Points(0.0f, 300.0f);
				LineSegement ls4 = new LineSegement(startLine4, endLine4);
				lsList.add(ls4);
				
				float aX1 = centerP.x + (radius * (float)(Math.cos(ANGLE_ADJUSTMENT)));
				float aY1 = centerP.y + (radius * (float)(Math.sin(ANGLE_ADJUSTMENT)));
				Points pArc1 = new Points(aX1, aY1);
				
				float aX2 = centerP.x + (radius * (float)(Math.cos(MAX_ANGLE - ANGLE_ADJUSTMENT)));
				float aY2 = centerP.y + (radius * (float)(Math.sin(MAX_ANGLE - ANGLE_ADJUSTMENT)));
				Points pArc2 = new Points(aX2, aY2);
				
				for(int l = 0; l < lsList.size(); l++)
				{
					List<Points> interP = getIntersectionArcLineSeg(centerP, radius, lsList.get(l).startP, lsList.get(l).endP, pArc1, pArc2);
					
					for(Points inter : interP)
					{						
						putMarkers(inter, false);
					}									
				}
				*/
				
				// ===================================================== //	
				
				double MAX_ANGLE = (180 * (float)(Math.PI/180));
				double ANGLE_ADJUSTMENT = -(20 * (float)(Math.PI/180));
				
				Points centerP = new Points(200.0f, 400.0f);
				float radius = 141.4f;		
				float tolerance = 0.5f; // 5 mm
				
				float aX1 = centerP.x + (radius * (float)(Math.cos(ANGLE_ADJUSTMENT)));
				float aY1 = centerP.y + (radius * (float)(Math.sin(ANGLE_ADJUSTMENT)));
				Points pArc1 = new Points(aX1, aY1);
				
				float aX2 = centerP.x + (radius * (float)(Math.cos(MAX_ANGLE - ANGLE_ADJUSTMENT)));
				float aY2 = centerP.y + (radius * (float)(Math.sin(MAX_ANGLE - ANGLE_ADJUSTMENT)));
				Points pArc2 = new Points(aX2, aY2);
			
				List<Points> interPList = new ArrayList<Points>();	
				
				for( float[][] fRects : furnRects)
				{
					List<Points> intList = getIntersectionArcRectangle(centerP, radius, fRects, pArc1, pArc2, tolerance);
					interPList.addAll(intList);
				}
				
				// ===================================================== //	
				
				long endTime = System.nanoTime();
				
				putMarkers(pArc1, true);
				putMarkers(pArc2, true);
				
				//putMarkers(minInter, true);
				
				JOptionPane.showMessageDialog(null, "Time : " + (endTime - startTime) + " ns \n");
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
		}
		
		public List<Points> getIntersectionArcRectangle(Points center, float rad, float[][] furnRect, Points arcP1, Points arcP2, float tolerance)
		{
			List<Points> retList = new ArrayList<Points>();			
			List<LineSegement> lsList = new ArrayList<LineSegement>();
			
			//JOptionPane.showMessageDialog(null,("furn : " + furnRect[0][0] + "," + furnRect[0][1] + " / " + furnRect[1][0] + "," + furnRect[1][1] + " / " + furnRect[2][0] + "," + furnRect[2][1] + " / " + furnRect[3][0] + "," + furnRect[3][1]));
			
			for(int f = 0; f < furnRect.length; f++)
			{
				Points startLine = new Points(furnRect[f][0], furnRect[f][1]);
				
				Points endLine = null;
				
				if(f == (furnRect.length - 1))
					endLine = new Points(furnRect[0][0], furnRect[0][1]);
				else
					endLine = new Points(furnRect[f+1][0], furnRect[f+1][1]);				
				
				LineSegement ls = new LineSegement(startLine, endLine);
				lsList.add(ls);
			}
			
			for(int l = 0; l < lsList.size(); l++)
			{
				Points startP = lsList.get(l).startP;
				Points endP = lsList.get(l).endP;
				
				List<Points> interP = getIntersectionArcLineSeg(center, rad, startP, endP, arcP1, arcP2);
				
				for(Points inter : interP)
				{		
					boolean bInBetween = checkPointInBetween(inter, startP, endP, tolerance);
					
					if(bInBetween)
					{
						retList.addAll(interP);
						putMarkers(inter, false);
					}
					//else
						//putMarkers(inter, true);
				}									
			}
			
			return retList;
		}
		
		public List<Points> getIntersectionArcLineSeg(Points center, float rad, Points startL, Points endL, Points arcP1, Points arcP2)
		{
			List<Points> retList = new ArrayList<Points>();
			
			List<Points> interList = getIntersectionCircleLine(center, rad, startL, endL);
			
			for(Points p : interList)
			{
				boolean bOnSameSide = checkPointOnSameSide(center, p, arcP1, arcP2);
				
				if(!bOnSameSide)
					retList.add(p);
			}		
			
			return retList;
		}		
		
		public List<Points> getIntersectionCircleLine(Points center, float rad, Points startL, Points endL)
		{
			List<Points> interList = new ArrayList<Points>();
			
			try
			{	
				if(endL.x == startL.x)
				{
					float dist = (float) Math.abs(startL.x - center.x);
							
					if(dist <= rad)
					{
						float x01 = startL.x;
						float y01 = center.y - (float)Math.sqrt((rad*rad) - (dist*dist));
						
						Points inter1 = new Points(x01, y01);
						interList.add(inter1);
						//putMarkers(inter1, false);
						
						float x02 = startL.x;
						float y02 = center.y + (float)Math.sqrt((rad*rad) - (dist*dist));
						
						Points inter2 = new Points(x02, y02);
						interList.add(inter2);
						//putMarkers(inter2, false);
					}
					//else : Line does not intersect with this circle
				}
				else
				{
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
						
						//putMarkers(inter, true);
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
				}				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -xxxxx- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
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
			
			for(Wall w: h.getWalls())
			{
				furnIds.add("wall_" + wallCount);				
				float[][] wRect = w.getPoints();
				
				List<Points> validPoints = new ArrayList<Points>();
						
				for(int ws = 0; ws < wRect.length; ws++)
				{
					Points p = new Points(wRect[ws][0], wRect[ws][1]);
					
					if(room.containsPoint(p.x, p.y, (ROOM_TOLERANCE * w.getThickness())))
						validPoints.add(p);
				}
				
				JOptionPane.showMessageDialog(null, validPoints.size());
						
				float[][] validRect = new float[validPoints.size()][2];
				
				for(int i = 0; i < validPoints.size(); i++)
				{
					validRect[i][0] = validPoints.get(i).x;
					validRect[i][1] = validPoints.get(i).y;
				}
				
				furnRects.add(validRect);
				furnThicks.add(w.getThickness());		
							
				wallCount++;
			}
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
