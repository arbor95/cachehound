package de.cachehound.exporter.gpx;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.jdom.Element;
import org.jdom.Namespace;

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
public class GpxDecoratorGroundspeak implements IGpxDecorator {
	public static final Namespace groundspeak = Namespace.getNamespace(
			"groundspeak", "http://www.groundspeak.com/cache/1/0");

	@Override
	public void decorateDomTree(Element doc, ICacheHolder ch) {
		// Guard: Only works for Caches, not for Waypoints
		if (!ch.isCacheWpt()) {
			return;
		}
		Element gpx = doc;

		// groundspeak:cache tags:
		Element cache = new Element("cache", groundspeak);
		cache.setAttribute("id", ch.getCacheID());

		cache.setAttribute("available", ch.isAvailable() ? "True" : "False");
		cache.setAttribute("archived", ch.isArchived() ? "True" : "False");
		gpx.getChildren().add(cache);

		Element gName = new Element("name", groundspeak);
		gName.setText(ch.getCacheName());
		cache.getChildren().add(gName);

		Element gPlaced = new Element("placed_by", groundspeak);
		gPlaced.setText(ch.getCacheOwner());
		cache.getChildren().add(gPlaced);

		Element gOwner = new Element("owner", groundspeak);
		gOwner.setText(ch.getCacheOwner());
		// Todo: hier müsste die orginal Id rein
		gOwner.setAttribute("id", "123456");
		cache.getChildren().add(gOwner);

		Element gType = new Element("type", groundspeak);
		gType.setText(ch.getType().getGcGpxString());
		cache.getChildren().add(gType);

		Element gContainer = new Element("container", groundspeak);
		gContainer.setText(ch.getCacheSize().getAsString());
		cache.getChildren().add(gContainer);

		Element gDifficulty = new Element("difficulty", groundspeak);
		gDifficulty.setText(ch.getDifficulty().getShortRepresentation());
		cache.getChildren().add(gDifficulty);

		Element gTerrain = new Element("terrain", groundspeak);
		gTerrain.setText(ch.getTerrain().getShortRepresentation());
		cache.getChildren().add(gTerrain);

		Element gCountry = new Element("country", groundspeak);
		gCountry.setText(ch.getDetails().getCountry());
		cache.getChildren().add(gCountry);

		Element gState = new Element("state", groundspeak);
		gState.setText(ch.getDetails().getState());
		cache.getChildren().add(gState);

		Element gShortDescription = new Element("short_description",
				groundspeak);
		if ("".equals(ch.getDetails().getShortDescription())) {
			gShortDescription.setText("\n");
		} else {
			gShortDescription.setText(ch.getDetails().getShortDescription());
		}

		gShortDescription.setAttribute("html", ch.isHTML() ? "True" : "False");
		cache.getChildren().add(gShortDescription);

		Element gLongDescription = new Element("long_description", groundspeak);
		gLongDescription.setText(ch.getDetails().getLongDescription());
		gLongDescription.setAttribute("html", ch.isHTML() ? "True" : "False");
		cache.getChildren().add(gLongDescription);

		Element gEncodedHints = new Element("encoded_hints", groundspeak);
		gEncodedHints.setText(Rot13.encodeRot13(ch.getDetails().getHints()));
		cache.getChildren().add(gEncodedHints);
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
