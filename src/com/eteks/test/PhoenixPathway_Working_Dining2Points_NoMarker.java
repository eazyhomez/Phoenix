package com.eteks.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.omg.CORBA.Request;

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
	
	public HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[4];
	
	public class RoomTestAction extends PluginAction 
	{		
		public Home home = null;
		public Room room = null;
		
		public Room foyer = null;
		
		public HomePieceOfFurniture dining = null;		
		public HomePieceOfFurniture entryDoor = null;
		
		public float ROOM_TOLERANCE = 0.51f;
		public float FURN_TOLERANCE = 0.51f;
		
		public float DINING_DISTANCE = 76.2f;  // 2.5 feet
		
		public double MAX_ANGLE = (180 * (float)(Math.PI/180));
		public double ANGLE_ADJUSTMENT = -(20 * (float)(Math.PI/180));
		
		public float radius = 75.0f;		
		public float tolerance = 20.0f; // 5 mm
		
		public boolean bStop = false;
		
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
		
		public class ArcSegement
		{
			Points startP;		// x, y
			Points endP;		// x, y
			
			public ArcSegement(Points sP, Points eP)
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
			
			getDiningRect();
			
			List<List<LineSegement>> masterNewSegList = new ArrayList<List<LineSegement>>();
			
			try
			{				
				storeAllFurnRects(home);
				storeAllWallRects(home);				
				markBoxes = getMarkerBoxes();

				long startTime = System.nanoTime();
				
				// ===================================================== //	
				float padding = 15.2f;	// 6 inches	
				float newArcLength = 50.0f;
				
				float[] startPoints = getStartingPoints();
				Points sP1 = new Points(startPoints[0], startPoints[1]);
				Points sP2 = new Points(startPoints[2], startPoints[3]);
				
				Points centerP = new Points(((sP1.x + sP2.x)/2), ((sP1.y + sP2.y)/2));
				//putMarkers(centerP, 1);				
				
				List<LineSegement> newSegList = runFirstLoop(centerP, radius, sP1, sP2, padding, newArcLength);
				masterNewSegList.add(newSegList);
				
				int loopCount = 0;				

				while(!bStop)
				{
					loopCount++;
					
					List<List<LineSegement>> nxtMasterNewSegList = new ArrayList<List<LineSegement>>();
					
					for(List<LineSegement> lsList : masterNewSegList)
					{						
						for(LineSegement ls : lsList)
						{
							Points midP = new Points(((ls.startP.x + ls.endP.x)/2), ((ls.startP.y + ls.endP.y)/2));
							
							boolean bInRoom = room.containsPoint(midP.x, midP.y, ROOM_TOLERANCE);
							
							if(bInRoom)
							{
								List<LineSegement> nxtSegList = runNextLoop(ls, radius, padding, centerP, newArcLength);
								
								if(bStop)
									break;
								
								if(nxtSegList.size() > 0)
									nxtMasterNewSegList.add(nxtSegList);
							}
						}
						
						if(bStop)
							break;
					}
					
					if(bStop)
						break;
					
					masterNewSegList = nxtMasterNewSegList;
				}
				
				masterNewSegList = new ArrayList<List<LineSegement>>();
				bStop = false;
				
				// ===================================================== //	
				
				long endTime = System.nanoTime();
				
				JOptionPane.showMessageDialog(null, "Time : " + (endTime - startTime) + " ns \n loopCount : " + loopCount);
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
		}
		
		public List<LineSegement> runFirstLoop(Points centerP, float rad, Points sP1, Points sP2, float padding, float newArcLength)
		{			
			List<LineSegement> newArcSegList = new ArrayList<LineSegement>();
			
			List<Points> arcP = generateStartArcPoints(sP1, sP2, rad, padding);
			
			if(arcP.size() > 1)
			{
				Points arcP1 = arcP.get(0);
				Points arcP2 = arcP.get(1);
				
				//putMarkers(arcP1, 0);
				//putMarkers(arcP2, 0);
				
				List<LineSegement> arcSegList = generateFreeArcSegs(centerP, arcP1, arcP2, rad);
				
				if(!bStop)
				{
					if(arcSegList.size() > 0)
					{
						/*for(LineSegement ls : arcSegList)
						{
							Points mp = new Points(((ls.startP.x + ls.endP.x)/2), ((ls.startP.y + ls.endP.y)/2));
							putMarkers(mp, 2);
						}*/
						
						newArcSegList = generateNextArcSegs(arcSegList, newArcLength);
					}
				}
			}
			
			return newArcSegList;
		}
		
		public List<LineSegement> runNextLoop(LineSegement ls, float rad, float h, Points prevCenter, float newArcLen)
		{
			List<LineSegement> newArcSegList = new ArrayList<LineSegement>();
					
			Points sP1 = new Points(ls.startP.x, ls.startP.y);
			Points sP2 = new Points(ls.endP.x, ls.endP.y);
			
			Points centerP = new Points(((sP1.x + sP2.x)/2), ((sP1.y + sP2.y)/2));
			//putMarkers(centerP, 2);	
			
			List<Points> arcP = generateNextArcPoints(sP1, sP2, rad, h, prevCenter);
			
			if(arcP.size() > 1)
			{
				Points arcP1 = arcP.get(0);
				Points arcP2 = arcP.get(1);
				
				//putMarkers(arcP1, 0);
				//putMarkers(arcP2, 0);
				
				List<LineSegement> arcSegList = generateFreeArcSegs(centerP, arcP1, arcP2, rad);
				
				if(!bStop)
				{
					if(arcSegList.size() > 0)
					{
						/*for(LineSegement nls : arcSegList)
						{
							Points mp = new Points(((nls.startP.x + nls.endP.x)/2), ((nls.startP.y + nls.endP.y)/2));
							putMarkers(mp, 2);
						}*/
						
						newArcSegList = generateNextArcSegs(arcSegList, newArcLen);	
					}
				}
			}
			
			return newArcSegList;
		}
		
		
		// TODO : Check for the distance between intersection points
		public boolean checkDining(List<Points> inPList)
		{
			boolean bRet = false;
			int count = 0;
			
			if(dining != null)
			{
				for(Points p : inPList)
				{
					boolean bInDining = dining.containsPoint(p.x, p.y, FURN_TOLERANCE);
					
					if(bInDining)
						count++;
					
					if(count >= 2)
					{
						bRet = true;
						break;
					}
				}
			}
			
			return bRet;
		}
		
		public boolean checkDiningReached(List<Points> inPList)
		{
			boolean bRet = false;
			int count = 0;
			
			if((dining != null) && (inPList.size() > 1))
			{
				Points refStart = inPList.get(0);
				
				for(int p = 0; p < inPList.size(); p++)
				{
					float dist = calcDistance(refStart, inPList.get(p));
					
					if(dist >= DINING_DISTANCE)
					{
						bRet = true;
						break;
					}
				}
			}
			
			return bRet;
		}
		
		public List<Points> sortPList(List<Points> interPList, Points ref)
		{
			List<Points> retPList = new ArrayList<Points>();
			TreeMap<Float, Points> pMap = new TreeMap<Float, Points>();
			
			for(Points p : interPList)
			{
				float dist = calcDistance(p, ref);
				pMap.put(dist, p);
			}
			
			Set<Float> keys = pMap.keySet();
			
			for(Float d : keys)
			{
				retPList.add(pMap.get(d));
			}
					
			return retPList;
		}
		
		public List<LineSegement> generateNextArcSegs(List<LineSegement> freeArcSegList, float reqLength)
		{
			List<LineSegement> retLSList = new ArrayList<LineSegement>();
			
			for(int f = 0; f < freeArcSegList.size(); f++)
			{
				LineSegement ls = freeArcSegList.get(f);
				
				int iter = new Float(calcDistance(ls.startP, ls.endP) / reqLength).intValue();
				
				Points prevPoint = ls.startP;
				
				for(int i = 0; i < iter; i++)
				{
					List<Points> interP = getIntersectionCircleLine(ls.startP, (reqLength *(i+1)), ls.startP, ls.endP);
					
					for(Points p : interP)
					{
						boolean bInBetween = checkPointInBetween(p, ls.startP, ls.endP, tolerance);
						
						if(bInBetween)
						{
							retLSList.add(new LineSegement(prevPoint, p));
							prevPoint = p;
							//putMarkers(p, 1);
						}
					}
				}				
			}
			
			return retLSList;
		}
		
		public List<LineSegement> generateFreeArcSegs(Points center, Points pArc1, Points pArc2, float rad)
		{
			List<LineSegement> arcSegList = new ArrayList<LineSegement>();
			
			List<Points> interPList = getIntersectionInHome(center, pArc1, pArc2, rad);
			//List<Points> interPList = getIntersectionInHomeWithDiningCheck(center, pArc1, pArc2, rad);
			
			/*for(Points p : interPList)
			{
				putMarkers(p, 3);
			}*/
						
			boolean bInDining = checkDining(interPList);
			
			if(!bInDining)
			{
				List<Points> sortedPList = sortPList(interPList, pArc1);
				List<Points> checkPList = new ArrayList<Points>();
				checkPList.add(pArc1);
				checkPList.addAll(sortedPList);
				checkPList.add(pArc2);		
				
				boolean bCheckP1 = checkPointBlocked(pArc1);
						
				if(bCheckP1)
				{
					for(int x = 1; (x+1) < checkPList.size();)
					{
						LineSegement freeAS = new LineSegement(checkPList.get(x), checkPList.get(x+1));
						arcSegList.add(freeAS);
						
						//putMarkers(checkPList.get(x), true);
						//putMarkers(checkPList.get(x+1), true);
						
						x += 2;
					}
				}
				else
				{
					for(int x = 0; (x+1) < checkPList.size();)
					{
						LineSegement freeAS = new LineSegement(checkPList.get(x), checkPList.get(x+1));
						arcSegList.add(freeAS);
						
						//putMarkers(checkPList.get(x), false);
						//putMarkers(checkPList.get(x+1), false);
						
						x += 2;
					}
				}
			}
			else
				bStop = true;
			
			return arcSegList;
			//JOptionPane.showMessageDialog(null, bCheckP1);			
		}
		
		public List<Points> getIntersectionInHome(Points center, Points pArc1, Points pArc2, float rad)
		{		
			List<Points> interPList = new ArrayList<Points>();	
			
			for(float[][] fRects : furnRects)
			{
				List<Points> intList = getIntersectionArcRectangle(center, radius, fRects, pArc1, pArc2, tolerance);
				interPList.addAll(intList);
			}
			
			return interPList;
		}
		
		public List<Points> getIntersectionInHomeWithDiningCheck(Points center, Points pArc1, Points pArc2, float rad)
		{		
			List<Points> interPList = new ArrayList<Points>();	
			
			for(int f = 0; f < furnRects.size(); f++)
			{
				List<Points> intList = getIntersectionArcRectangle(center, radius, furnRects.get(f), pArc1, pArc2, tolerance);
				
				if(furnIds.get(f).equalsIgnoreCase("DiningRect"))
				{
					boolean bCheck = checkDiningReached(intList);
					
					if(bCheck)
					{
						bStop = true;
						break;
					}
					else
						interPList.addAll(intList);
				}
			}
			
			return interPList;
		}
		
		public boolean checkPointBlocked(Points test)
		{
			boolean bIsInside = false;
			
			for(HomePieceOfFurniture hpf : home.getFurniture())
			{
				String fName = hpf.getName();
				
				if(!fName.equals("boxred") && !fName.equals("boxgreen") )
				{
					boolean bCheck1 = hpf.containsPoint(test.x, test.y, FURN_TOLERANCE);
					
					if(bCheck1)
					{
						bIsInside = true;
						break;
					}
				}
			}
			
			//JOptionPane.showMessageDialog(null, "1 :" +  bIsInside);
			
			if(!bIsInside)
			{
				for(Wall w : home.getWalls())
				{
					boolean bCheck2 = w.containsPoint(test.x, test.y, FURN_TOLERANCE);
					
					if(bCheck2)
					{
						bIsInside = true;
						break;
					}
				}
				
				//JOptionPane.showMessageDialog(null, "2 :" +  bIsInside);
			}
			
			if(!bIsInside)
			{
				boolean bCheck3 = room.containsPoint(test.x, test.y, ROOM_TOLERANCE);
			
				if(!bCheck3)
				{
					bIsInside = true;
				}
				
				//JOptionPane.showMessageDialog(null, "3 :" +  bIsInside);
			}
				
			return bIsInside;
		}
		
		public List<Points> getIntersectionArcRectangle(Points center, float rad, float[][] furnRect, Points arcP1, Points arcP2, float tolerance)
		{
			List<Points> retList = new ArrayList<Points>();			
			List<LineSegement> lsList = new ArrayList<LineSegement>();
			
			//JOptionPane.showMessageDialog(null,("furn : " + furnRect[0][0] + "," + furnRect[0][1] + " / " + furnRect[1][0] + "," + furnRect[1][1] + " / " + furnRect[2][0] + "," + furnRect[2][1] + " / " + furnRect[3][0] + "," + furnRect[3][1]));
			
			if(furnRect.length == 2)
			{
				Points startLine = new Points(furnRect[0][0], furnRect[0][1]);
				Points endLine = new Points(furnRect[1][0], furnRect[1][1]);
				
				LineSegement ls = new LineSegement(startLine, endLine);
				lsList.add(ls);
			}
			else
			{			
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
						retList.add(inter);
						//putMarkers(inter, false);
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
		
		public void getDiningRect()
		{			
			for(HomePieceOfFurniture hpf : home.getFurniture())
			{			
				if(hpf.getName().equalsIgnoreCase("diningrect"))
				{
					dining = hpf;					
				}
			}
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
			
			//JOptionPane.showMessageDialog(null, bIsInside + " -> " + test.x + ", " + test.y);
			
			return bIsInside;			
			//return room.containsPoint(test.x, test.y, ROOM_TOLERANCE);
		}
		
		// ======================= DEBUG FUNCTIONS ======================= //
		
		public void putMarkers(Points p, int indx)
		{
			HomePieceOfFurniture box = null;
			
			if(indx == 0)
				box = markBoxes[0].clone();
			else if(indx == 1)
				box = markBoxes[1].clone();
			else if(indx == 2)
				box = markBoxes[2].clone();
			else if(indx == 3)
				box = markBoxes[3].clone();
			
			box.setX(p.x);
			box.setY(p.y);
			home.addPieceOfFurniture(box);
		}
		
		public HomePieceOfFurniture[] getMarkerBoxes()
		{
			HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[4];
			int count = 0;
			
			List<FurnitureCategory> fCatg = getUserPreferences().getFurnitureCatalog().getCategories();
			
			for(int c = 0; c < fCatg.size(); c++ )
			{
				if(count >= 4)
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
					else if(catPOF.get(p).getName().equals("boxblue"))
					{
						markBoxes[2] = new HomePieceOfFurniture(catPOF.get(p));
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxyellow"))
					{
						markBoxes[3] = new HomePieceOfFurniture(catPOF.get(p));
						count++;
					}
					
					if(count >= 4)
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
