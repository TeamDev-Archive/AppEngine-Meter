package com.teamdev.appengine.meter;

import org.apache.http.Header;
import org.apache.http.message.BasicLineParser;
import org.apache.jmeter.samplers.SampleResult;

public class CostCalculator {

	private static final String ESTIMATED_CPM_DOLLARS_HEADER = "X-AppEngine-Estimated-CPM-US-Dollars";
	
	private static final String RESOURCE_USAGE_HEADER = "X-AppEngine-Resource-Usage";
	
	private String label;

	private double sum = 0;

	private int count = 0;

	public CostCalculator(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void addSample(SampleResult sample) {
		Double cost = getEstimatedDollars(sample);
		if(cost == null) {
			return;
		}
		count++;
		sum += cost;
	}
	
	private static Double getEstimatedDollars(SampleResult sample) {
		String responseHeaders = sample.getResponseHeaders();
		if (!responseHeaders.contains(ESTIMATED_CPM_DOLLARS_HEADER)) {
			return null;
		}
		String[] headerStrings = responseHeaders.split("\\r?\\n");
		for (String headerString : headerStrings) {
			if (!headerString.contains(ESTIMATED_CPM_DOLLARS_HEADER)) {
				continue;
			}
			Header header = BasicLineParser.parseHeader(headerString, BasicLineParser.DEFAULT);
			if (header.getName().equals(ESTIMATED_CPM_DOLLARS_HEADER)) {
				return Double.parseDouble(header.getValue().substring(1));
			}
		}
		return null;
	}

	public int getCount() {
        return count;
    } 
	
	public double getAverage() {
		return sum / count;
	}
	
	public double getSum() {
		return sum;
	}

	public static boolean hasCost(SampleResult sample) {
		return getEstimatedDollars(sample) != null;
	}

}
