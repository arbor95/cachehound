package de.cachehound.util.gcvote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.CacheType;

import CacheWolf.beans.CacheHolder;

public class GcVoteImporter {

	private static Logger logger = LoggerFactory
			.getLogger(GcVoteImporter.class);

	private String user = "undefined";
	private String password = "";

	// connection to server
	private static String VOTE_SERVER_BASE = "http://dosensuche.de/GCVote";
	private static String GET_VOTES = VOTE_SERVER_BASE + "/getVotes.php";
	private static String SET_VOTE = VOTE_SERVER_BASE + "/setVote.php";
	private static String LIST_VOTES = VOTE_SERVER_BASE + "/listUserVotes.php";

	private void refreshVotes(Collection<ICacheHolder> caches) {
		ArrayList<String> ids = new ArrayList<String>();
		int i = 0;
		for (ICacheHolder cache : caches) {
			if (cache.isCacheWpt()) {
				ids.add(cache.getWayPoint());
			}
		}
		
		// tmporär
		System.out.println(getVoting(ids.toArray(new String[] {})));
	}
	
	private String getVoting(String[] gcNumbers) {

		StringBuilder sb = new StringBuilder();
		for (String id : gcNumbers) {
			sb.append(id).append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), "");

		String data;
		try {

			data = "version=" + "cachehound" + "&userName=" + user + "&waypoints="
					+ sb.toString() + "&password=" + password;
//			data = URLEncoder.encode(data, "UTF-8");

			// Send data
			URL url = new URL(GET_VOTES);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn
					.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String line;
			sb = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				sb.append(line).append("\n");
			}
			wr.close();
			rd.close();
		} catch (Exception e) {
			logger.error("Failure at asking gcVote.", e);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		GcVoteImporter importer = new GcVoteImporter();
		
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
		importer.refreshVotes(list);
		
		System.out.println( importer.getVoting(new String[] {"GC1KHXZ", "GC1PQ47", "GC2A58A"}));
		
	}
}
