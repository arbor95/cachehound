package de.cachehound.beans;

/**
 * This class is an immutable for holding the data from GCVote.
 * It has to filled with the data in the constructor.
 * 
 * @author tweety
 *
 */
public class GcVote {

	private final double average;
	private final double median;
	private final double myVote;

	private final int voteCount;
	private final int vote1;
	private final int vote2;
	private final int vote3;
	private final int vote4;
	private final int vote5;

	/**
	 * @param average 	The average from gcVote
	 * @param median	The median from gcVote
	 * @param myVote	The voting the user was made from gcVote
	 * @param voteCount	The counting of votes from gcVote 
	 * @param vote1		The counting of votes with 1 (or 1.5) stars from gcVote
	 * @param vote2		The counting of votes with 2 (or 2.5) stars from gcVote
	 * @param vote3		The counting of votes with 3 (or 3.5) stars from gcVote
	 * @param vote4		The counting of votes with 4 (or 4.5) stars from gcVote
	 * @param vote5		The counting of votes with 5 stars from gcVote
	 */
	public GcVote(double average, double median, double myVote, int voteCount,
			int vote1, int vote2, int vote3, int vote4, int vote5) {
		super();
		this.median = median;
		this.average = average;
		this.myVote = myVote;
		this.voteCount = voteCount;
		this.vote1 = vote1;
		this.vote2 = vote2;
		this.vote3 = vote3;
		this.vote4 = vote4;
		this.vote5 = vote5;
	}

	@Override
	public String toString() {
		return "GcVote [average=" + average + ", median=" + median
				+ ", myVote=" + myVote + ", vote1=" + vote1 + ", vote2="
				+ vote2 + ", vote3=" + vote3 + ", vote4=" + vote4 + ", vote5="
				+ vote5 + ", voteCount=" + voteCount + "]";
	}
	
	public double getMedian() {
		return median;
	}

	public double getAverage() {
		return average;
	}

	public double getMyVote() {
		return myVote;
	}

	public int getVoteCount() {
		return voteCount;
	}

	public int getVote1() {
		return vote1;
	}

	public int getVote2() {
		return vote2;
	}

	public int getVote3() {
		return vote3;
	}

	public int getVote4() {
		return vote4;
	}

	public int getVote5() {
		return vote5;
	}

}
