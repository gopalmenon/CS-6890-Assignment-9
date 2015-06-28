package menon.cs6890.assignment9;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vkedco.nlp.earlyparser.ParseTree;
import org.vkedco.nlp.earlyparser.Parser;

public class MainClass {
	
	private Map<String, String> oppositeDirections = null;
	
	private static final String PDDL_FILE_1 = "usu_quad_prob01.pddl";
	private static final String PDDL_FILE_2 = "usu_quad_prob02.pddl";
	
	public MainClass() {
		this.oppositeDirections = new HashMap<String, String>();
		this.oppositeDirections.put("NORTH", "SOUTH");
		this.oppositeDirections.put("EAST", "WEST");
		this.oppositeDirections.put("SOUTH", "NORTH");
		this.oppositeDirections.put("WEST", "EAST");
	}
	
	public static void main(String[] args) {
		
		MainClass mainClass = new MainClass();
		
		String grammarFile = "directionsGrammar.txt";
		
		List<String> directions = mainClass.getDirectionsFromTextFile("routeDirections1.txt");		
		String parseTreeDirections1 = mainClass.getParseTreeString(grammarFile, directions);
		ExtractedDirectionComponents extractedDirectionComponents1 = mainClass.extractDirectionComponents(parseTreeDirections1);
		
		directions = mainClass.getDirectionsFromTextFile("routeDirections2.txt");
		String parseTreeDirections2 = mainClass.getParseTreeString(grammarFile, directions);
		ExtractedDirectionComponents extractedDirectionComponents2 = mainClass.extractDirectionComponents(parseTreeDirections2);
		
		Map<Integer, String> pddlFiles = mainClass.getPddlNavigationProblemFiles(extractedDirectionComponents1, extractedDirectionComponents2);
		
		mainClass.insertWordsIntoTextFile(pddlFiles.get(Integer.valueOf(1)), PDDL_FILE_1);
		mainClass.insertWordsIntoTextFile(pddlFiles.get(Integer.valueOf(2)), PDDL_FILE_2);
        
	}
	
	
	/**
	 * @param inputFilePath
	 * @return the list of words in the text file having one word per line
	 */
	private List<String> getDirectionsFromTextFile(String inputFilePath) {
		
		String words = null;
		List<String> returnValue = new ArrayList<String>();
		try {
			BufferedReader textFileReader = new BufferedReader(new FileReader(inputFilePath));
			words = textFileReader.readLine().toUpperCase();
			while(words != null) {
				if (words.trim().length() > 0) {
					returnValue.add(words.trim());
				}
				words = textFileReader.readLine();
			}
			textFileReader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File " + inputFilePath + " was not found.");
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IOException thrown while reading file " + inputFilePath + ".");
			e.printStackTrace();
			return null;
		}
		
		return returnValue;
	}

	private String getParseTreeString(String grammarFile, List<String> directions) {
		
		String [] terminalsInDirections = null;
		StringBuffer buffer = new StringBuffer();
		
		if (directions.size() > 0) {
			for (String directionsComponent : directions) {
				if (directionsComponent.length() > 0) {
					terminalsInDirections = directionsComponent.split("\\s");
					System.out.println("Going to parse: \"" + directionsComponent + "\" with " + terminalsInDirections.length + " terminals.");
			        Parser epr = Parser.factory(grammarFile, terminalsInDirections.length);
			        ArrayList<ParseTree> ptrees = epr.parse(directionsComponent);
			        epr.displayParseTrees(ptrees, buffer);
			        epr.displayChart();
				}
			}
		}
		
		return buffer.toString();
	}
	
	private ExtractedDirectionComponents extractDirectionComponents(String parseTreeDirections) {
		
		StringBuffer leaveBuilding = new StringBuffer();
		StringBuffer arriveAtBuilding = new StringBuffer();
		StringBuffer walkInDirection = new StringBuffer();
		StringBuffer turnToDirection = new StringBuffer(); 
		
		//Look for the building to leave
		String[] tokens = parseTreeDirections.split("\\s");
		boolean firstBuildingFound = false, firstDirectionFound = false;
		int parseTreeOffset = 0, firstBuildingOffset = 0, secondBuildingOffset = 0, firstDirectionOffset = 0, secondDirectionOffset = 0;
		String token = null;
		
		for (parseTreeOffset = 0; parseTreeOffset < tokens.length; ++parseTreeOffset) {
			
			token = tokens[parseTreeOffset].trim();
			if ("EDF1".equals(token.trim())) {
				if (firstBuildingFound) {
					secondBuildingOffset = parseTreeOffset;
				} else {
					firstBuildingOffset = parseTreeOffset;
				}
				firstBuildingFound = true;
			} else if ("DIRECTION".equals(token.trim())) {
				if (firstDirectionFound) {
					secondDirectionOffset = parseTreeOffset;
				} else {
					firstDirectionOffset = parseTreeOffset;
				}
				firstDirectionFound = true;
			}
			
		}
		
		leaveBuilding.append(tokens[firstBuildingOffset + 2].trim());
		if ("EDF3".equals(tokens[firstBuildingOffset + 5].trim())) {
			leaveBuilding.append(tokens[firstBuildingOffset + 7].trim());
		} else {
			leaveBuilding.append(tokens[firstBuildingOffset + 7].trim());
			leaveBuilding.append(tokens[firstBuildingOffset + 10].trim());
		}
		
		arriveAtBuilding.append(tokens[secondBuildingOffset + 2].trim());
		if ("EDF3".equals(tokens[secondBuildingOffset + 5].trim())) {
			arriveAtBuilding.append(tokens[secondBuildingOffset + 7].trim());
		} else {
			arriveAtBuilding.append(tokens[secondBuildingOffset + 7].trim());
			arriveAtBuilding.append(tokens[secondBuildingOffset + 10].trim());
		}
		
		walkInDirection.append(tokens[firstDirectionOffset + 2].trim());
		
		turnToDirection.append(tokens[secondDirectionOffset + 2].trim());
		
		return new ExtractedDirectionComponents(leaveBuilding.toString(), arriveAtBuilding.toString(), walkInDirection.toString(), turnToDirection.toString());
		
	}
	
	private Map<Integer, String> getPddlNavigationProblemFiles(ExtractedDirectionComponents extractedDirectionComponents1, ExtractedDirectionComponents extractedDirectionComponents2) {
		
		Map<Integer, String> returnValue = new HashMap<Integer, String>();
		
		returnValue.put(Integer.valueOf(1), getPddlFile(1, extractedDirectionComponents1.getLeaveBuilding(), extractedDirectionComponents1.getArriveAtBuilding(), extractedDirectionComponents1, extractedDirectionComponents2));
		returnValue.put(Integer.valueOf(2), getPddlFile(2, extractedDirectionComponents2.getLeaveBuilding(), extractedDirectionComponents2.getArriveAtBuilding(), extractedDirectionComponents1, extractedDirectionComponents2));
		
		return returnValue;
	}
	
	private String getPddlFile(int fileNumber,String fromBuilding, String toBuilding, ExtractedDirectionComponents extractedDirectionComponents1, ExtractedDirectionComponents extractedDirectionComponents2) {
		
		StringBuffer returnValue = new StringBuffer();
		
		returnValue.append("(define (problem usu_quad_prob0").append(fileNumber).append(")\n");
		returnValue.append("(:domain usu-main-quad)\n");
		returnValue.append("(:objects ").append(extractedDirectionComponents1.getLeaveBuilding()).append(" ").append(extractedDirectionComponents1.getArriveAtBuilding()).append(" ").append(extractedDirectionComponents2.getLeaveBuilding()).append(" ").append(extractedDirectionComponents2.getArriveAtBuilding()).append(" ").append("MiddleOfQuad EAST NORTH SOUTH WEST Agent01)\n");
		returnValue.append("(:init\n");
		returnValue.append("(building ").append(extractedDirectionComponents1.getLeaveBuilding()).append(") ");
		returnValue.append("(building ").append(extractedDirectionComponents1.getArriveAtBuilding()).append(") ");
		returnValue.append("(building ").append(extractedDirectionComponents2.getLeaveBuilding()).append(") ");
		returnValue.append("(building ").append(extractedDirectionComponents2.getArriveAtBuilding()).append(") \n");
		returnValue.append("(place MiddleOfQuad)  (direction EAST) (direction NORTH) (direction SOUTH) (direction WEST) \n");
		returnValue.append("(agent Agent01) (at Agent01 ").append(fromBuilding).append(") (facing Agent01 ");
		if (fromBuilding.equals(extractedDirectionComponents1.getLeaveBuilding())) {
			returnValue.append(extractedDirectionComponents1.getWalkInDirection());
		} else {
			returnValue.append(extractedDirectionComponents2.getWalkInDirection());
		}
		returnValue.append(")\n");
		returnValue.append("(path ").append(extractedDirectionComponents1.getLeaveBuilding()).append(" MiddleOfQuad ").append(extractedDirectionComponents1.getWalkInDirection()).append(")\n");
		returnValue.append("(path MiddleOfQuad ").append(extractedDirectionComponents1.getLeaveBuilding()).append(" ").append(this.oppositeDirections.get(extractedDirectionComponents1.getWalkInDirection())).append(")\n");
		returnValue.append("(path MiddleOfQuad ").append(extractedDirectionComponents1.getArriveAtBuilding()).append(" ").append(extractedDirectionComponents1.getTurnToDirection()).append(")\n");
		returnValue.append("(path ").append(extractedDirectionComponents1.getArriveAtBuilding()).append(" MiddleOfQuad ").append(this.oppositeDirections.get(extractedDirectionComponents1.getTurnToDirection())).append(")\n");
		returnValue.append("(path ").append(extractedDirectionComponents2.getLeaveBuilding()).append(" MiddleOfQuad ").append(extractedDirectionComponents2.getWalkInDirection()).append(")\n");
		returnValue.append("(path MiddleOfQuad ").append(extractedDirectionComponents2.getLeaveBuilding()).append(" ").append(this.oppositeDirections.get(extractedDirectionComponents2.getWalkInDirection())).append(")\n");
		returnValue.append("(path MiddleOfQuad ").append(extractedDirectionComponents2.getArriveAtBuilding()).append(" ").append(extractedDirectionComponents2.getTurnToDirection()).append(")\n");
		returnValue.append("(path ").append(extractedDirectionComponents2.getArriveAtBuilding()).append(" MiddleOfQuad ").append(this.oppositeDirections.get(extractedDirectionComponents2.getTurnToDirection())).append("))\n");
		returnValue.append("(:goal (and (at Agent01 ").append(toBuilding).append(") (facing Agent01 ");
		if (toBuilding.equals(extractedDirectionComponents1.getArriveAtBuilding())) {
			returnValue.append(extractedDirectionComponents1.getTurnToDirection());
		} else {
			returnValue.append(extractedDirectionComponents2.getTurnToDirection());
		}
		returnValue.append("))))");
		
		return returnValue.toString();
	}
	
	/**
	 * @param words
	 * @param outputFilePath
	 * @return true if able to insert the words into the text file
	 */
	private boolean insertWordsIntoTextFile(String ppdlFileContents, String outputFileName) {
		
	    Path path = Paths.get(outputFileName);
        try {
    	    BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
			writer.write(ppdlFileContents);
    	    writer.close();
		} catch (IOException e) {
			System.err.println("IOException thrown while trying to write to file " + outputFileName);
			e.printStackTrace();
			return false;
		}

        return true;
	}
}
