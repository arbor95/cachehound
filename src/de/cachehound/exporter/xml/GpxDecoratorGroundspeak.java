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
import de.cachehound.beans.LogList;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;
import de.cachehound.util.Rot13;

/**
 * This class adds to the generated Dom-Tree the groundspeak:cache Tag. It
 * doesn't contains the logs or Travelbugs - there for other Decorators should
 * be used.
 * 
 * @author tweety
 */
public class GpxDecoratorGroundspeak implements IDomDecorator {

	private static Logger logger = LoggerFactory
			.getLogger(GpxDecoratorGroundspeak.class);

	@Override
	public void decorateDomTree(Document doc, ICacheHolder ch) {
		// Guard: Only works for Caches, not for Waypoints
		if (!ch.isCacheWpt()) {
			return;
		}
		NodeList nodeList = doc.getElementsByTagName("wpt");
		if (nodeList.getLength() != 1) {
			logger
					.error("GpxDecoratorGroundspeak doesn't find exacly one wpt-Node");
			throw new RuntimeException(
					"GpxDecoratorGroundspeak doesn't find exacly one wpt-Node");
		}
		Node gpx = nodeList.item(0);

		// groundspeak:cache tags:
		Element cache = doc.createElement("groundspeak:cache");
		cache.setAttribute("id", ch.getCacheID());
		cache.setAttribute("xmlns:groundspeak",
				"http://www.groundspeak.com/cache/1/0");
		cache.setAttribute("available", ch.isAvailable() ? "True" : "False");
		cache.setAttribute("archived", ch.isArchived() ? "True" : "False");
		gpx.appendChild(cache);

		Element gName = doc.createElement("groundspeak:name");
		gName.setTextContent(ch.getCacheName());
		cache.appendChild(gName);

		Element gPlaced = doc.createElement("groundspeak:placed_by");
		gPlaced.setTextContent(ch.getCacheOwner());
		cache.appendChild(gPlaced);

		Element gOwner = doc.createElement("groundspeak:owner");
		gOwner.setTextContent(ch.getCacheOwner());
		// Todo: hier müsste die orginal Id rein
		gOwner.setAttribute("id", "123456");
		cache.appendChild(gOwner);

		Element gType = doc.createElement("groundspeak:type");
		gType.setTextContent(ch.getType().getGcGpxString());
		cache.appendChild(gType);

		Element gContainer = doc.createElement("groundspeak:container");
		gContainer.setTextContent(ch.getCacheSize().getAsString());
		cache.appendChild(gContainer);

		Element gDifficulty = doc.createElement("groundspeak:difficulty");
		gDifficulty.setTextContent(ch.getDifficulty().getShortRepresentation());
		cache.appendChild(gDifficulty);

		Element gTerrain = doc.createElement("groundspeak:terrain");
		gTerrain.setTextContent(ch.getTerrain().getShortRepresentation());
		cache.appendChild(gTerrain);

		Element gCountry = doc.createElement("groundspeak:country");
		gCountry.setTextContent(ch.getDetails().getCountry());
		cache.appendChild(gCountry);

		Element gState = doc.createElement("groundspeak:state");
		gState.setTextContent(ch.getDetails().getState());
		cache.appendChild(gState);

		Element gShortDescription = doc
				.createElement("groundspeak:short_description");
		if ("".equals(ch.getDetails().getShortDescription())) {
			gShortDescription.setTextContent("\n");
		} else {
			gShortDescription.setTextContent(ch.getDetails().getShortDescription());
		}
		
		gShortDescription.setAttribute("html", ch.isHTML() ? "True" : "False");
		cache.appendChild(gShortDescription);

		Element gLongDescription = doc
				.createElement("groundspeak:long_description");
		gLongDescription.setTextContent(ch.getDetails().getLongDescription());
		gLongDescription.setAttribute("html", ch.isHTML() ? "True" : "False");
		cache.appendChild(gLongDescription);

		Element gEncodedHints = doc.createElement("groundspeak:encoded_hints");
		gEncodedHints.setTextContent(Rot13.encodeRot13(ch.getDetails()
				.getHints()));
		cache.appendChild(gEncodedHints);
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

		try {
			exp.doit(caches);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}

}
