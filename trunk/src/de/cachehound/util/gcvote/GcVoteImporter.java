package de.cachehound.util.gcvote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.GcVote;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.gui.interfaces.AbstractProgressTask;

/**
 * This class Imports the Data from gcvote.com.
 * 
 * Because it's needing no internal state, it's made with a singleton pattern like a common service.
 * 
 * It's based on the gcvote 2.0b Version.
 * 
 * @author tweety
 *
 */

public class GcVoteImporter extends AbstractProgressTask {

	private static Logger logger = LoggerFactory
	.getLogger(GcVoteImporter.class);
	
	private static GcVoteImporter instance = new GcVoteImporter();
	
	private String user = "undefined";
	private String password = "";

	// connection to server
	private static String VOTE_SERVER_BASE = "http://dosensuche.de/GCVote";
	private static String GET_VOTES = VOTE_SERVER_BASE + "/getVotes.php";
//	private static String SET_VOTE = VOTE_SERVER_BASE + "/setVote.php";
//	private static String LIST_VOTES = VOTE_SERVER_BASE + "/listUserVotes.php";

	private GcVoteImporter() {
		// singleton
		setHeadLine("GcVote Import");
	}
	
	public static GcVoteImporter getInstance() {
		return instance;
	}
	
	/**
	 * Refreshes the data from GcVote for all caches in the collection.
	 * 
	 * @param caches
	 */
	public void refreshVotes(Collection<? extends ICacheHolder> caches) {
		setProgress(0);
		logger.debug("Refreshing GcVote Data ...");
		setText("Preparing Request ...");
		HashMap<String, ICacheHolder> cacheMap = new HashMap<String, ICacheHolder>();
		for (ICacheHolder cache : caches) {
			if (cache.isCacheWpt()) {
				cacheMap.put(cache.getWayPoint(), cache);
			}
		}
		InputStream inXml;
		try {
			setText("Asking gcVote Server ...");
			inXml = getVoting(cacheMap.keySet());
			parseXML(inXml, cacheMap);
		} catch (IOException e) {
			logger.error("Failre at receiving gcVote.", e);
		} catch (ParserConfigurationException e) {
			logger.error("Failure at parsing gcVote.", e); 
		} catch (SAXException e) {
			logger.error("Failure at parsing gcVote.", e); 
		}
		logger.debug("... GcVote data refreshed.");
	}

	/**
	 * This Method gets the Voting from the gcVote Server. It returns the
	 * votings as (XML) InputStream.
	 * 
	 * @param gcNumbers
	 *            A set of Strings (GCxxxx) with the caches which should be
	 *            asked.
	 * @return The votings form the gcVote Server.
	 * @throws IOException
	 */
	private InputStream getVoting(Set<String> gcNumbers) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String id : gcNumbers) {
			sb.append(id).append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), "");

		String data;
		data = "version=" + "cachehound" + "&userName=" + user + "&waypoints="
				+ sb.toString() + "&password="
				+ URLEncoder.encode(password, "UTF-8");

		// Send data
		URL url = new URL(GET_VOTES);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		wr.close();
		// Get the response
		return conn.getInputStream();
	}

	/**
	 * This method parses the InputStream from GcVote with DOM an looks for errors.
	 * If there is no error it calls parseVotings to get all the voting datas.
	 *    
	 * @param in
	 * @param geoCaches
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void parseXML(InputStream in, Map<String, ICacheHolder> geoCaches)
			throws ParserConfigurationException, SAXException, IOException {
		setText("Parsing GcVote data ...");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(in);
		in.close();

		NodeList errors = document.getElementsByTagName("errorstring");
		for (int i = 0; i < errors.getLength(); i++) {
			if (!"".equals(errors.item(i).getTextContent().trim())) {
				logger.error("Failure from gcVote Server: {}", errors.item(i)
						.getTextContent().trim());
				return; // Dann die Daten lieber nicht verarbeiten.
			}

		}
		parseVotings(document, geoCaches);
	}

	/**
	 * This Method parses the votings in the given (DOM)-Dokument an fills the geoCaches with them.
	 * @param document
	 * @param geoCaches
	 */
	private void parseVotings(Document document,
			Map<String, ICacheHolder> geoCaches) {
		NodeList votes = document.getElementsByTagName("vote");

		// A lot of Variables for holding the data from one vote.
		Node vote;
		String gcNumber;
		double average;
		double median;
		double myVote;
		int voteCount;
		int vote1;
		int vote2;
		int vote3;
		int vote4;
		int vote5;

		// a loop for every Cache in the voting results
		for (int i = 0; i < votes.getLength(); i++) {
			setProgress(((double) i) / votes.getLength());
			vote = votes.item(i);

			gcNumber = vote.getAttributes().getNamedItem("waypoint")
					.getTextContent();
			average = Double.valueOf(vote.getAttributes().getNamedItem(
					"voteAvg").getTextContent());
			median = Double.valueOf(vote.getAttributes().getNamedItem(
					"voteMedian").getTextContent());
			myVote = Double.valueOf(vote.getAttributes().getNamedItem(
					"voteUser").getTextContent());

			voteCount = Integer.valueOf(vote.getAttributes().getNamedItem(
					"voteCnt").getTextContent());
			vote1 = Integer.valueOf(vote.getAttributes().getNamedItem("vote1")
					.getTextContent());
			vote2 = Integer.valueOf(vote.getAttributes().getNamedItem("vote2")
					.getTextContent());
			vote3 = Integer.valueOf(vote.getAttributes().getNamedItem("vote3")
					.getTextContent());
			vote4 = Integer.valueOf(vote.getAttributes().getNamedItem("vote4")
					.getTextContent());
			vote5 = Integer.valueOf(vote.getAttributes().getNamedItem("vote5")
					.getTextContent());

			GcVote gcVote = new GcVote(average, median, myVote, voteCount,
					vote1, vote2, vote3, vote4, vote5);
			geoCaches.get(gcNumber).setGcVote(gcVote);

			logger.debug("Set voting for {}: {}", gcNumber, gcVote);
		}
	}

	/**
	 * Just a small test and learn method for this class. 
	 * @param args
	 */
	public static void main(String[] args) {

		ICacheHolder ch = new CacheHolderDummy() {
			public String getWayPoint() {
				return "GC1KHXZ";
			}

			public boolean isCacheWpt() {
				return true;
			}
		};
		List<ICacheHolder> list = new ArrayList<ICacheHolder>();
		list.add(ch);
		
		GcVoteImporter importer = GcVoteImporter.getInstance();
		importer.refreshVotes(list);
	}
}
