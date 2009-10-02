package de.cachehound.util.gcvote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

			//data = "version=2.0b&userName=undefined&waypoints=GC11XJD,GC13YE0,GC13ZX6,GC14WFG,GC163K1,GC17N9N,GC1NZPP,GC1RQB4,GC1XRJ3&password=vote4ever";
			
			System.out.println("data:\n" + data);
			
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
		
		System.out.println( importer.getVoting(new String[] {"GC1KHXZ", "GC1PQ47", "GC2A58A"}));
		
	}
}
