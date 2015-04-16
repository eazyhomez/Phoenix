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
		
		public Room foyer = null;
		public HomePieceOfFurniture entryDoor = null;
		
		public float ROOM_TOLERANCE = 0.51f;
		public double MAX_ANGLE = (180 * (float)(Math.PI/180));
		public double ANGLE_ADJUSTMENT = -(20 * (float)(Math.PI/180));
		
		public float radius = 200.0f;		
		public float tolerance = 0.5f; // 5 mm
		
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
				
				//Points centerP = getStartingPoints();
				//List<Points> interPList = getIntersectionInHome(centerP, radius, ANGLE_ADJUSTMENT);				
				/*
				Points p1 = new Points(0.0f, 0.0f);
				putMarkers(p1, false);
				
				Points p21 = new Points(100.0f, -100.0f);
				putMarkers(p21, true);
				String debugStr = "" + getStartAngle(p1, p21);
				
				debugStr += "\n" + getStartAngle(p21, p1) + "\n------\n";
				
				Points p22 = new Points(-100.0f, -100.0f);
				putMarkers(p22, true);
				debugStr += "" + getStartAngle(p1, p22);
				
				debugStr += "\n" + getStartAngle(p22, p1) + "\n------\n";
				
				Points p23 = new Points(-100.0f, 100.0f);
				putMarkers(p23, true);
				debugStr += "" + getStartAngle(p1, p23);
				
				debugStr += "\n" + getStartAngle(p23, p1) + "\n------\n";
				
				Points p24 = new Points(100.0f, 100.0f);
				putMarkers(p24, true);
				debugStr += "" + getStartAngle(p1, p24);
				
				debugStr += "\n" + getStartAngle(p24, p1) + "\n------\n";

				JOptionPane.showMessageDialog(null, debugStr);
				*/		
				
				// ===================================================== //	
				/*
				float[] startPoints = getStartingPoints();
				
				Points sP1 = new Points(startPoints[0], startPoints[1]);
				Points sP2 = new Points(startPoints[2], startPoints[3]);
				
				Points centerP = new Points(((sP1.x + sP2.x)/2), ((sP1.y + sP2.y)/2));
				putMarkers(centerP, false);
				
				float padding = 15.2f;
				
				List<Points> arcP = generateStartArcPoints(sP1, sP2, radius, padding);
				
				if(arcP.size() > 1)
				{
					putMarkers(arcP.get(0), true);
					putMarkers(arcP.get(1), true);
				}
				
				//JOptionPane.showMessageDialog(null, arcP.size());
				*/
				
				// ===================================================== //	
				
				Points prevCenter = new Points(700.0f, 500.0f);
				putMarkers(prevCenter, true);
				
				Points arcP1 = new Points(900.0f, 500.0f);
				putMarkers(arcP1, true);
				
				Points arcP2 = new Points(700.0f, 700.0f);
				putMarkers(arcP2, true);
				
				float padding = 15.2f;
				
				List<Points> nxtArcP = generateNextArcPoints(arcP1, arcP2, radius, padding, prevCenter);
				
				if(nxtArcP.size() > 1)
				{
					putMarkers(nxtArcP.get(0), false);
					putMarkers(nxtArcP.get(1), false);
				}
				
				// ===================================================== //	
				
				long endTime = System.nanoTime();
				
				//putMarkers(centerP, true);
				//putMarkers(pArc1, true);
				//putMarkers(pArc2, true);
				
				//JOptionPane.showMessageDialog(null, "Time : " + (endTime - startTime) + " ns \n");
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
		}
		
		public List<Points> getIntersectionInHome(Points center, float rad, double angAdjust)
		{
			float aX1 = center.x + (radius * (float)(Math.cos(angAdjust)));
			float aY1 = center.y + (radius * (float)(Math.sin(angAdjust)));
			Points pArc1 = new Points(aX1, aY1);
			
			float aX2 = center.x + (radius * (float)(Math.cos(MAX_ANGLE - angAdjust)));
			float aY2 = center.y + (radius * (float)(Math.sin(MAX_ANGLE - angAdjust)));
			Points pArc2 = new Points(aX2, aY2);
			
			List<Points> interPList = new ArrayList<Points>();	
			
			for( float[][] fRects : furnRects)
			{
				List<Points> intList = getIntersectionArcRectangle(center, radius, fRects, pArc1, pArc2, tolerance);
				interPList.addAll(intList);
			}
			
			return interPList;
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
		
		public float getStartAngle(Points p1, Points p2)
		{
			float ang = (float)Math.atan((p2.y - p1.y) / (p2.x - p1.x) );
			float angRad = ang * (float) (180.0f / Math.PI);
					
			return angRad;
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
				
				//JOptionPane.showMessageDialog(null, validPoints.size());
						
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
		
		public float[] getStartingPoints()
		{
			float[] startPoints = new float[4];
			
			for(Room r : home.getRooms())
			{	
				String roomName = (r.getName() != null) ? r.getName().trim() : "";
				
				if(!roomName.isEmpty() && roomName.equalsIgnoreCase("foyer"))
				{
					foyer = r;
					
					float[][] roomRect = r.getPoints();
					
					if(roomRect.length > 1)
					{
						startPoints[0] = roomRect[0][0];
						startPoints[1] = roomRect[0][1];
						startPoints[2] = roomRect[1][0];
						startPoints[3] = roomRect[1][1];						
					}
				}
			}
			
			return startPoints;
		}
		
		public float[] getFoyerOppPoints()
		{
			float[] foyerPoints = new float[4];
			
			for(Room r : home.getRooms())
			{	
				String roomName = (r.getName() != null) ? r.getName().trim() : "";
				
				if(!roomName.isEmpty() && roomName.equalsIgnoreCase("foyer"))
				{
					foyer = r;
					
					float[][] roomRect = r.getPoints();
					
					if(roomRect.length > 1)
					{
						foyerPoints[0] = roomRect[2][0];
						foyerPoints[1] = roomRect[2][1];
						foyerPoints[2] = roomRect[3][0];
						foyerPoints[3] = roomRect[3][1];						
					}
				}
			}
			
			return foyerPoints;
		}
		
		public List<Points> generateStartArcPoints(Points pS1, Points pS2, float rad, float h)
		{
			List<Points> retPList = new ArrayList<Points>();
			
			Points center = new Points(((pS1.x + pS2.x) / 2),((pS1.y + pS2.y) / 2));
			
			List<Points> extPoints = getIntersectionCircleLine(center, rad, pS1, pS2);
			
			Points newA = extPoints.get(0);
			Points newB = extPoints.get(1);
			
			float omega = (newB.y - newA.y); 
			float delta = (newB.x - newA.x);
			
			float pX1 = newA.x + ((h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY1 = newA.y + ((-h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p1 = new Points(pX1, pY1);
			
			float pX2 = newA.x + ((-h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY2 = newA.y +  ((h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p2 = new Points(pX2, pY2);
			
			float pX3 = newB.x + ((h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY3 = newB.y + ((-h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p3 = new Points(pX3, pY3);
			
			float pX4 = newB.x + ((-h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY4 = newB.y +  ((h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p4 = new Points(pX4, pY4);
			
			float[] foyerPoints = getFoyerOppPoints();
			
			Points fP1 = new Points(foyerPoints[0], foyerPoints[1]);
			Points fP2 = new Points(foyerPoints[2], foyerPoints[3]);
			
			Points fMid = new Points(((fP1.x + fP2.x)/2), ((fP1.y + fP2.y)/2));
			
			if(!checkPointOnSameSide(fMid, p1, pS1, pS2))
			{
				retPList.add(new Points(pX1, pY1));
				//putMarkers(new Points(pX1, pY1), false);
			}
			
			if(!checkPointOnSameSide(fMid, p2, pS1, pS2))
			{
				retPList.add(new Points(pX2, pY2));
				//putMarkers(new Points(pX2, pY2), false);
			}
			
			if(!checkPointOnSameSide(fMid, p3, pS1, pS2))
			{
				retPList.add(new Points(pX3, pY3));
				//putMarkers(new Points(pX3, pY3), false);
			}
			
			if(!checkPointOnSameSide(fMid, p4, pS1, pS2))
			{
				retPList.add(new Points(pX4, pY4));
				//putMarkers(new Points(pX4, pY4), false);
			}
			
			return retPList;
		}
		
		public List<Points> generateNextArcPoints(Points pS1, Points pS2, float rad, float h, Points prevCenter)
		{
			List<Points> retPList = new ArrayList<Points>();
					
			Points center = new Points(((pS1.x + pS2.x) / 2),((pS1.y + pS2.y) / 2));
			
			List<Points> extPoints = getIntersectionCircleLine(center, rad, pS1, pS2);
			
			Points newA = extPoints.get(0);
			Points newB = extPoints.get(1);
			
			float omega = (newB.y - newA.y); 
			float delta = (newB.x - newA.x);
			
			float pX1 = newA.x + ((h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY1 = newA.y + ((-h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p1 = new Points(pX1, pY1);
			
			float pX2 = newA.x + ((-h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY2 = newA.y +  ((h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p2 = new Points(pX2, pY2);
			
			float pX3 = newB.x + ((h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY3 = newB.y + ((-h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p3 = new Points(pX3, pY3);
			
			float pX4 = newB.x + ((-h*omega) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			float pY4 = newB.y +  ((h*delta) / ((float)Math.sqrt((omega*omega) + (delta*delta))));
			Points p4 = new Points(pX4, pY4);
			
			if(!checkPointOnSameSide(prevCenter, p1, pS1, pS2))
			{
				retPList.add(new Points(pX1, pY1));
			}
			
			if(!checkPointOnSameSide(prevCenter, p2, pS1, pS2))
			{
				retPList.add(new Points(pX2, pY2));
			}
			
			if(!checkPointOnSameSide(prevCenter, p3, pS1, pS2))
			{
				retPList.add(new Points(pX3, pY3));
			}
			
			if(!checkPointOnSameSide(prevCenter, p4, pS1, pS2))
			{
				retPList.add(new Points(pX4, pY4));
			}
			
			return retPList;			
		}
		
		
		public boolean checkInRoom(Room r, Points test)
		{
			boolean bIsInside = true;
			float[][] roomRect = r.getPoints();
			
			Points rCenter = new Points(r.getXCenter(), r.getYCenter());

			for(int x = 0; x < roomRect.length; x++)
			{
				Points pS1 = new Points(roomRect[x][0], roomRect[x][1]);				
				Points pS2 = null;
				
				if(x == (roomRect.length - 1))
					pS2 = new Points(roomRect[0][0], roomRect[0][1]);
				else
					pS2 = new Points(roomRect[x+1][0], roomRect[x+1][1]);	
				
				boolean bCheck = checkPointOnSameSide(rCenter, test, pS1, pS2);
				
				bIsInside = (bIsInside && bCheck);
			}
			
			JOptionPane.showMessageDialog(null, bIsInside + " -> " + test.x + ", " + test.y);
			
			return bIsInside;			
			//return room.containsPoint(test.x, test.y, ROOM_TOLERANCE);
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
