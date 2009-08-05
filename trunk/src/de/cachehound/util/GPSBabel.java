package de.cachehound.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;

public class GPSBabel {
	private static Logger logger = LoggerFactory.getLogger(GPSBabel.class);

	public enum Filetype {
		geo, gpx, garmin;
	}

	public static boolean isPresent() {
		try {
			ProcessBuilder pb = new ProcessBuilder("gpsbabel", "-V");
			Process p = pb.start();
			p.waitFor();
		} catch (java.io.IOException e) {
			// this is expected if gpsbabel is not found.
			return false;
		} catch (InterruptedException e) {
			logger
					.error(
							"InterruptedException thrown while testing for gpsbabel",
							e);
			return false;
		}
		return true;
	}

	public static void convert(String intype, String infile, String outtype,
			String outfile, String... opts) throws IOException {
		// FIXME: Find a better way to do this.

		List<String> args = new ArrayList<String>();

		args.add("gpsbabel");
		args.addAll(Arrays.asList(opts));
		for (String arg : Arrays.asList(Global.getPref().garminGPSBabelOptions
				.split(" +"))) {
			// FIXME: pref.garminGPSBabelOptions should really be an array
			// or done away with completely
			if (!arg.equals("")) {
				args.add(arg);
			}
		}
		args.add("-i");
		args.add(intype);
		args.add("-f");
		args.add(infile);
		args.add("-o");
		args.add(outtype);
		args.add("-F");
		args.add(outfile);

		logger.debug(args.toString());

		Process p;
		boolean error = false;
		try {
			p = Runtime.getRuntime().exec(args.toArray(new String[0]));
			p.waitFor();
		} catch (IOException e) {
			logger.error("Error calling gpsbabel", e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("Error calling gpsbabel", e);
			throw new IOException(e);
		}
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(p
				.getErrorStream()));
		String errorLine;
		while ((errorLine = errorReader.readLine()) != null) {
			logger.error("gpsbabel: " + errorLine);
			error = true;
		}
		if (error) {
			throw new IOException("gpsbabel wrote to stderr");
		}
	}

	public static void convert(Filetype intype, String infile,
			Filetype outtype, String outfile, String... opts)
			throws IOException {
		convert(intype.toString(), infile, outtype.toString(), outfile, opts);
	}
}