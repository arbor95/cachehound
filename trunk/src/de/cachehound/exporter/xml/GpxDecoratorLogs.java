package de.cachehound.exporter.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheImages;
import CacheWolf.beans.TravelbugList;
import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.beans.ICacheHolderDetail;
import de.cachehound.beans.Log;
import de.cachehound.beans.LogList;
import de.cachehound.factory.LogFactory;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.LogType;
import de.cachehound.types.Terrain;

public class GpxDecoratorLogs implements IDomDecorator {

	private static Logger logger = LoggerFactory
			.getLogger(GpxDecoratorLogs.class);
	
	private int countLogs;
	
	public GpxDecoratorLogs() {
		setCountLogs(5);
	}
	
	public GpxDecoratorLogs(int countLogs) {
		this.setCountLogs(countLogs);
	}
	
	@Override
	public void decorateDomTree(Document doc, ICacheHolder ch) {
		// Guard: Only works for Caches, not for Waypoints
		if (!ch.isCacheWpt()) {
			return;
		}
		NodeList nodeList = doc.getElementsByTagName("groundspeak:cache");
		if (nodeList.getLength() != 1) {
			logger
					.error("GpxDecoratorLogs doesn't find exacly one groundspeak:cache-Node");
			throw new RuntimeException(
					"GpxDecoratorLogs doesn't find exacly one groundspeak:cache-Node");
		}
		Node cache = nodeList.item(0);

		Element gLogs = doc.createElement("groundspeak:logs");
		cache.appendChild(gLogs);
		
		int logCount = 0;
		for (Log log : ch.getDetails().getCacheLogs()) {
			logCount++;
			if (logCount > countLogs ) {
				break;
			}
			
			Element gLog = doc.createElement("groundspeak:log");
			if ("".equals(log.getId())) {
				gLog.setAttribute("id", "123456");
			} else {
				gLog.setAttribute("id", log.getId());	
			}
			gLogs.appendChild(gLog);
			
			Element gDate = doc.createElement("groundspeak:date");
			gDate.setTextContent(log.getDate()+ "T19:00:00");
			gLog.appendChild(gDate);
			
			Element gType = doc.createElement("groundspeak:type");
			gType.setTextContent(log.getLogType().toGcComType());
			gLog.appendChild(gType);
			
			Element gFinder = doc.createElement("groundspeak:finder");
			gFinder.setTextContent(log.getLogger());
			// TODO: LoggerId noch richtig setzen.
			if (log.getLoggerId().equals("")) {
				gFinder.setAttribute("id", "123456");	
			} else {
				gFinder.setAttribute("id", log.getLoggerId());
			}
			
			gLog.appendChild(gFinder);
			
			Element gText = doc.createElement("groundspeak:text");
			gText.setTextContent(log.getMessage());
			// TODO: Naja, Alle Logs sind halt nicht verschlüsselt ... ist ja auch nicht wirklich "schlimm" 
			gText.setAttribute("encoded", "False");
			gLog.appendChild(gText);
		}
	}
	
	// Ab hier ist manueller Testcode - zum ausprobieren.
	public static void main(String... args) {
		ICacheHolder cache = new CacheHolderDummy() {
			@Override
			public String getCacheName() {
				return "TestCache";
			}

			@Override
			public String getCacheOwner() {
				return "TestOwner";
			}

			@Override
			public String getWayPoint() {
				return "GC12345";
			}

			@Override
			public CWPoint getPos() {
				return new CWPoint(-52.1, 8.12345678);
			}

			@Override
			public Difficulty getDifficulty() {
				return Difficulty.DIFFICULTY_1_0;
			}

			@Override
			public Terrain getTerrain() {
				return Terrain.TERRAIN_1_5;
			}

			@Override
			public CacheType getType() {
				return CacheType.MULTI;
			}

			@Override
			public String getCacheID() {
				return "1234567";
			}
			
			@Override
			public boolean isCacheWpt() {
				return true;
			}
			
			@Override
			public CacheSize getCacheSize() {
				return CacheSize.MICRO;
			}

			@Override
			public boolean isHTML() {
				return true;
			}
			
			@Override
			public ICacheHolderDetail getDetails() {
				return new ICacheHolderDetail() {
					@Override
					public String getLongDescription() {
						return "Beschreibung des Wegpunktes";
					}

					@Override
					public String getUrl() {
						return "http://www.geocaching.com/seek/wpt.aspx?WID=a70708a9-dd9a-4375-8b57-afec8d547ae0";
					}
					@Override
					public String getCountry() {
						return "Germany";
					}

					@Override
					public String getState() {
						return "Hamburg";
					}

					@Override
					public String getShortDescription() {
						return "<br/> Short <br/>";
					}

					@Override
					public String getHints() {
						return "[Stage1]NOPQRST";
					}

					@Override
					public LogList getCacheLogs() {
						LogList logList = new LogList();
						Log log1 = LogFactory.getInstance().createLog(LogType.FOUND,
								"2009-06-09T19:00:00", "CacherAAA", "Ich bin der Log text.", "1234", "5678");
						logList.add(log1);
						Log log2 = LogFactory.getInstance().createLog(LogType.DID_NOT_FOUND,
								"2009-06-09T19:00:00", "CacherBBB", "Ich bin nicht gefunden.", "12345", "67890");
						logList.add(log2);
						return logList;
					}

					@Override
					public TravelbugList getTravelbugs() {
						return null;
					}

					@Override
					public CacheImages getImages() {
						return null;
					}

				};
			}
		};
		
		ICacheHolder cache2 = new CacheHolderDummy() {
			@Override
			public String getCacheName() {
				return "TestCache2";
			}

			@Override
			public String getCacheOwner() {
				return "TestOwner2";
			}

			@Override
			public String getWayPoint() {
				return "GC13456";
			}

			@Override
			public CWPoint getPos() {
				return new CWPoint(53.12345678, 10.12345678);
			}

			@Override
			public Difficulty getDifficulty() {
				return Difficulty.DIFFICULTY_4_0;
			}

			@Override
			public Terrain getTerrain() {
				return Terrain.TERRAIN_4_0;
			}

			@Override
			public CacheType getType() {
				return CacheType.QUESTION;
			}

			@Override
			public CacheSize getCacheSize() {
				return CacheSize.REGULAR;
			}

			@Override
			public ICacheHolderDetail getDetails() {
				return new ICacheHolderDetail() {
					@Override
					public String getLongDescription() {
						return "Mal <ganz ganz fieser C&de! in Däütsch";
					}

					@Override
					public String getUrl() {
						return "http://www.öpnv-karte.de";
					}
					@Override
					public String getCountry() {
						return "Germany";
					}

					@Override
					public String getState() {
						return "Hamburg";
					}

					@Override
					public String getShortDescription() {
						return null;
					}

					@Override
					public String getHints() {
						return null;
					}

					@Override
					public LogList getCacheLogs() {
						return null;
					}

					@Override
					public TravelbugList getTravelbugs() {
						return null;
					}

					@Override
					public CacheImages getImages() {
						return null;
					}
				};
			}
		};
		
		Collection<ICacheHolder> caches = new ArrayList<ICacheHolder>();
		caches.add(cache2);
		caches.add(cache);

		StringWriter sw = new StringWriter();
		GpxExporter exp = new GpxExporter(sw);
		exp.addDecorator(new GpxDecoratorGroundspeak());
		exp.addDecorator(new GpxDecoratorLogs());

		try {
			exp.doit(caches);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}

	public void setCountLogs(int countLogs) {
		this.countLogs = countLogs;
	}

	public int getCountLogs() {
		return countLogs;
	}
	
}
