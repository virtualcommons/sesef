package edu.asu.commons.command;

public abstract class OrderedCommand implements Command {
	
	private volatile static long hash = 0;
	
	private final long creationTime = System.nanoTime();
	
	private final long ordinal = hash++;
	
	public int compareTo(OrderedCommand command) {
		int comparison = compare(creationTime, command.creationTime);
		if (comparison == 0) {
			return compare(ordinal, command.ordinal);
		}
		return comparison;
	}
	
	private int compare(long a, long b) {
		return (a > b) ? 1 : (a == b) ? 0 : -1;
	}

}
