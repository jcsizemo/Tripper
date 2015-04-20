package com.sizemore.trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Tripper {
	
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
	
	public static final int CHAR_ZERO = 48;
	
	public Vector<String> createTrips(int population) {
		
		Vector<String> trips = new Vector<String>();
		
		for (int i = 0; i < population; i++) {
			StringBuilder trip = new StringBuilder();
			LinkedList<String> landmarks = new LinkedList<String>(Tripper.landmarks);
			
			while (!landmarks.isEmpty()) {
				int index = (int) (Math.random() * landmarks.size());
				char landmark = (char) (index + CHAR_ZERO);
				trip.append(landmark);
				landmarks.remove(index);
			}
			trips.add(trip.toString());
		}
		return trips;
	}
	
	public String spawn(String a, String b) {
		
		Set<Character> added = new HashSet<Character>();
		StringBuilder child = new StringBuilder();
		
		for (int i = 0; i < a.length(); i++) {
			char landmarkA = a.charAt(i);
			char landmarkB = b.charAt(i);
			char chosen;
			double choice = Math.random();
			
			if (added.contains(landmarkA) && added.contains(landmarkB)) continue;
			else if (added.contains(landmarkA)) chosen = landmarkB;
			else if (added.contains(landmarkB)) chosen = landmarkA;
			else if (choice < 0.5) chosen = landmarkA;
			else chosen = landmarkB;
			
			child.append(chosen);
			added.add(chosen);
		}
		
		LinkedList<String> leftovers = new LinkedList<String>(Tripper.landmarks);
		leftovers.removeAll(added);
		while (!leftovers.isEmpty()) {
			int index = (int) (Math.random() * leftovers.size());
			child.append((char) (index + CHAR_ZERO));
			leftovers.remove(index);
		}
		return child.toString();
	}
	
	public URL constructURL(char origin, char destination) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(BASE);
		sb = sb.append("origin=")
				.append(landmarks.get((int) (origin - CHAR_ZERO)))
				.append("&destination=")
				.append(landmarks.get((int) (destination - CHAR_ZERO)));
				//.append(Tripper.KEY);
		String url = sb.toString();
		url = url.replaceAll(" ", "%20");
		return new URL(url);
	}
	
	public int distance(char start, char end) {
		int fitness = 0;
		try {
			URL url = constructURL(start,end);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = br.readLine())) {
				if ("               \"distance\" : {".equals(line)) {
					line = br.readLine().trim();
					line = line.substring(line.indexOf('\"'));
					line = line.substring(0, line.indexOf('m'));
					if (line.contains(",")) line = line.substring(0, line.indexOf(',')) + line.substring(line.indexOf(',') + 1, line.length());
					fitness = Integer.parseInt(line);
					int x = 1;
					x = x + 1;
				}
			}
		}
		catch (MalformedURLException mue) {
			mue.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		routeCache.put(""+start+end, fitness);
		routeCache.put(""+end+start, fitness);
		return fitness;
	}
	
	public String mutate(String trip) {
		int mutations = (int) (Math.random() / 0.33);
		char[] landmarks = trip.toCharArray();
		
		for (int i = 0; i < mutations; i++) {
			int i1 = (int) (Math.random() * trip.length());
			int i2 = (int) (Math.random() * trip.length());
			char temp = (char) (i1 + CHAR_ZERO);
			landmarks[i1] = landmarks[i2];
			landmarks[i2] = temp;
		}
		return new String(landmarks);
	}
	
	public int fitness(String trip) {
		
		if (routeCache.containsKey(trip)) return routeCache.get(trip);
		
		char[] landmarks = trip.toCharArray();
		
		char begin = landmarks[0];
		int fitness = 0;
		
		for (int i = 1; i <= landmarks.length; i++) {
			char start = landmarks[i-1];
			char end;
			if (i != landmarks.length) end = landmarks[i];
			else end = begin;
			
			if (routeCache.containsKey(""+start+end))  fitness += routeCache.get(""+start+end);
			else if (routeCache.containsKey(""+end+start))  fitness += routeCache.get(""+end+start);
			else {
				fitness += distance(start, end);
			}
		}
		
		routeCache.put(trip, fitness);
		return fitness;
	}
	
	Comparator<String> compare = new Comparator<String>() {
		@Override
		public int compare(String t1, String t2) {
			if (fitness(t1) <= fitness(t2)) return -1;
			else return 1;
		}
	};
	
	public Tripper() {
		
		Vector<String> trips = createTrips(1000);
		
		Vector<String> nextGeneration = new Vector<String>();
		for (int i = 0; i < trips.size(); i++) {
			int i1 = (int) (Math.random() * trips.size());
			int i2 = (int) (Math.random() * trips.size());
			while (i2 == i1) i2 = (int) (Math.random() * trips.size());
			
			String child = spawn(trips.get(i1), trips.get(i2));
			child = mutate(child);
			nextGeneration.add(child);
			Collections.sort(trips,compare);
			Collections.sort(nextGeneration,compare);
			// merge lists here
		}
	}
	
	public static void main(String[] args) {
		new Tripper();
	}
	
	class Pair<T> {
		
		T first;
		T second;
		
		Pair(T first, T second) {
			this.first = first;
			this.second = second;
		}
	}
}
