package com.sizemore.trip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

public class Tripper implements Runnable {
	
	public static final String KEY = "&key=AIzaSyDT42XjrqOrIwTuzNejOtAMHqBapG8KK6U";
	public static final String BASE = "https://maps.googleapis.com/maps/api/directions/json?";
	
	public Map<String,Integer> routeCache = new HashMap<String,Integer>();
	
	public static final LinkedList<String> landmarks = new LinkedList<String>(
			Arrays.asList(new String[]{
					"Seattle",
					"Portland",
					"Grand Canyon",
					"Las Vegas",
					"Yosemite National Park",
					"Vancouver",
					"Niagara Falls",
					"Key West",
					"New Orleans",
					"Nantucket"
			}));
	
	
	public String constructURL(String origin, String destination) {
		StringBuilder sb = new StringBuilder(BASE);
		sb = sb.append("origin=")
				.append(origin)
				.append("&destination=")
				.append(destination)
				.append(Tripper.KEY);
		String url = sb.toString();
		url = url.replaceAll(" ", "%20")
				.replaceAll("\"", "%22")
				.replaceAll("<", "%3C")
				.replaceAll(">", "%3E")
				.replaceAll("#", "%23")
				.replaceAll("%", "%25")
				.replaceAll("|", "%7C");
		return url;
	}
	
	public Vector<LinkedList<String>> createTrips(int population) {
		
		Vector<LinkedList<String>> trips = new Vector<LinkedList<String>>();
		
		for (int i = 0; i < population; i++) {
			LinkedList<String> trip = new LinkedList<String>();
			LinkedList<String> landmarks = new LinkedList<String>(Tripper.landmarks);
			
			while (!landmarks.isEmpty()) {
				int index = (int) (Math.random() * landmarks.size());
				trip.add(landmarks.get(index));
				landmarks.remove(index);
			}
			trips.add(trip);
		}
		return trips;
	}
	
	public LinkedList<String> spawn(LinkedList<String> a, LinkedList<String> b) {
		LinkedList<String> child = new LinkedList<String>();
		for (int i = 0; i < a.size(); i++) {
			String landmarkA = a.get(i);
			String landmarkB = b.get(i);
			double choice = Math.random();
			
			if (child.contains(landmarkA) && child.contains(landmarkB)) continue;
			else if (child.contains(landmarkA)) child.add(landmarkB);
			else if (child.contains(landmarkB)) child.add(landmarkA);
			else if (choice < 0.5) child.add(landmarkA);
			else child.add(landmarkB);
		}
		
		LinkedList<String> leftovers = new LinkedList<String>(Tripper.landmarks);
		leftovers.removeAll(child);
		while (!leftovers.isEmpty()) {
			int index = (int) (Math.random() * leftovers.size());
			child.add(leftovers.get(index));
			leftovers.remove(index);
		}
		return child;
	}
	
	public LinkedList<String> mutate(LinkedList<String> trip) {
		int mutations = (int) (Math.random() / 0.33);
		
		for (int i = 0; i < mutations; i++) {
			int i1 = (int) (Math.random() * trip.size());
			int i2 = (int) (Math.random() * trip.size());
			String temp = trip.get(i1);
			trip.set(i1, trip.get(i2));
			trip.set(i2, temp);
		}
		return trip;
	}
	
	public int getDistance(String url) {
		try {
			InputStream response = new URL(url).openStream();
			response.
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int fitness(LinkedList<String> trip) {
		
		String begin = trip.get(0);
		int fitness = 0;
		
		for (int i = 1; i <= trip.size(); i++) {
			String start = trip.get(i-1);
			String end;
			if (i != trip.size()) end = trip.get(i);
			else end = begin;
			
			if (routeCache.containsKey(start+end))  fitness += routeCache.get(start+end);
			else if (routeCache.containsKey(end+start))  fitness += routeCache.get(end+start);
			else {
				String url = constructURL(start,end);
				fitness += getDistance(url);
				routeCache.put(start+end, fitness);
				routeCache.put(end+start, fitness);
			}
		}
		return fitness;
	}
	
	Comparator<LinkedList<String>> test = new Comparator<LinkedList<String>>() {

		@Override
		public int compare(LinkedList<String> o1, LinkedList<String> o2) {
			if (fitness(o1) <= fitness(o2)) return -1;
			else return 1;
		}
		
	};
	
	public Tripper() {
		
		Vector<LinkedList<String>> trips = createTrips(1000);
		
		Vector<LinkedList<String>> nextGeneration = new Vector<LinkedList<String>>();
		for (int i = 0; i < trips.size(); i++) {
			int i1 = (int) (Math.random() * trips.size());
			int i2 = (int) (Math.random() * trips.size());
			while (i2 == i1) i2 = (int) (Math.random() * trips.size());
			
			LinkedList<String> child = spawn(trips.get(i1), trips.get(i2));
			child = mutate(child);
			nextGeneration.add(child);
			Collections.sort(trips,test);
			Collections.sort(nextGeneration,test);
		}
	}
	
	public static void main(String[] args) {
		new Tripper();
	}

	@Override
	public void run() {
		
		
	}
}