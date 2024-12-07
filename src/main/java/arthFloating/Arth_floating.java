/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package arthFloating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Mahmoud
 */

public class Arth_floating {

    public static String turnToBinary(double doubleDecimal) {
        String doubleBinary = "";

        // int i = 0;
        while (doubleDecimal != 0/* && i < 16 */) {
            doubleDecimal *= 2;
            
            if (doubleDecimal >= 1) {
                doubleDecimal--;
                doubleBinary += "1";
            } else {
                doubleBinary += "0";
            }
            // i++;
        }
        // System.out.println(doubleBinary);
        return doubleBinary;

    }

    public static double turnToDecimal(String doubleBin) {
       double doubleDecimal = 0.0;

       for (int i = 0; i < doubleBin.length(); i++) {
           if ('1' == doubleBin.charAt(i)) {
               doubleDecimal += Math.pow(2, (i+1)*-1);
           }
       }

    //    System.out.println(doubleDecimal);
       return doubleDecimal;

    }

    public static void writeToBinFile(String doubleBinary, String outputFileName) {

        // write compressed bits to the bin file
        // System.out.print("Compressed binary file name: ");
        // String binaryFileName = consoleScanner.next() + ".bin";
        try (FileOutputStream binaryWriter = new FileOutputStream(outputFileName + ".bin")) {
        // calc padding len
        int paddingLength = 8 - (doubleBinary.length() % 8);
        if (paddingLength == 8) paddingLength = 0; // if size is a multiple of 8 -> no padding

        // save padding len in first byte
        binaryWriter.write(paddingLength);
        // do the padding for as many required to be a multiple of 8
        while (doubleBinary.length() % 8 != 0) {
            doubleBinary += "0";
        }

        // write to txt file for preview
        FileWriter out = new FileWriter("doublebin.txt");
        out.write(doubleBinary);
        out.close();

        // write data in bin file
        for (int i = 0; i < doubleBinary.length(); i += 8) {
            String byteString = doubleBinary.substring(i, i + 8);
            int byteValue = Integer.parseInt(byteString, 2);
            binaryWriter.write(byteValue);
        }

        // System.out.println("Compressed binary data saved to " + "doublebin.bin");

    } catch(Exception e) {
        System.out.println("didnt find file");
        
    }
}

    public static String readFromBinFile(String inputToDecompress) {
        // reading from binary file
        String doubleDecimal = "";
        try (FileInputStream binaryReader = new FileInputStream(inputToDecompress +".bin")) {
            int byteValue;
            // read padding length (first byte in binary file)
            int paddingLength = binaryReader.read(); // First byte is the padding length

            // read rest of compressed data from the bin file
            while ((byteValue = binaryReader.read()) != -1) {
                String byteString = String.format("%8s", Integer.toBinaryString(byteValue & 0xFF)).replace(' ', '0');
                doubleDecimal += byteString;
            }
            // System.out.println(" binary double read successfully.");

            // remove extra bits used for padding
            doubleDecimal = doubleDecimal.substring(0, doubleDecimal.length() - paddingLength);

        } catch (IOException e) {
            System.out.println("didnt find filexx");
            // System.out.println("Couldn't read from binary file: " + e.getMessage());
        }

        return doubleDecimal;
    }

    public static String readInput(String inputFileName) {
        File input = new File(inputFileName + ".txt");
        String inputData = "";
        try {
            Scanner inscan = new Scanner(input);
            while(inscan.hasNextLine()) {
                inputData += inscan.nextLine();
                inputData += "$";
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("didnt find file");
            System.out.println("can not open file");
        }

        return inputData;
    }

    public static void writeProbMap(Map<Character, Double> probMap) {
        try {

            FileWriter output = new FileWriter("probMap.txt");

            for (Map.Entry<Character, Double> entry : probMap.entrySet()) {
                String line = entry.getKey().toString() + " " + entry.getValue() + "\n";
                output.write(line);
            }

            output.close();

        } catch (IOException e) {
            System.out.println("didnt find file");
            
        }
        
    }

    public static Map<Character, CharEntry> readProbMap() {

        Map<Character, CharEntry> rangesMap = new HashMap<>();
        Map<Character, Double> probMap = new HashMap<>();

        Character chr;
        double prob = 0.0;


        try {
            Scanner input = new Scanner(new File("probMap.txt"));

            while (input.hasNextLine()) {
                String line = input.nextLine();
                chr = line.charAt(0);

                prob = Double.parseDouble(line.substring(2));

                probMap.put(chr, prob);
            }

            double currLower = 0.0;
            for (Map.Entry<Character, Double> entry : probMap.entrySet()) {
                CharEntry newCharEntry = new CharEntry(currLower, currLower + entry.getValue());
                currLower += entry.getValue();

                rangesMap.put(entry.getKey(), newCharEntry);
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("didnt find file");
            
        }

        return rangesMap;
    }

    public static double compress(String rawData) {

        double globalLower = 0;
        double globalUpper = 1;

        Map<Character, Integer> freqMap = new HashMap<>();
        Map<Character, Double> probMap = new HashMap<>();
        Map<Character, CharEntry> rangesMap = new HashMap<>();

        for (int i = 0; i < rawData.length(); i++) {

            if (freqMap.get(rawData.charAt(i)) == null) {
                freqMap.put(rawData.charAt(i), 1);
            } else {
                freqMap.put(rawData.charAt(i), freqMap.get(rawData.charAt(i)) + 1);
            }
        }

        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            probMap.put(entry.getKey(), (double)entry.getValue()/rawData.length());
        }
        // probMap.put('A', 0.8);
        // probMap.put('B', 0.02);
        // probMap.put('C', 0.18);

        double currLower = 0.0;
        for (Map.Entry<Character, Double> entry : probMap.entrySet()) {
            CharEntry newCharEntry = new CharEntry(currLower, currLower + entry.getValue());
            currLower += entry.getValue();

            rangesMap.put(entry.getKey(), newCharEntry);
        }

        
        // for (Map.Entry<Character, CharEntry> entry : rangesMap.entrySet()) {
        //     System.out.println(entry.getKey() + "   " + entry.getValue().lower + "->" + entry.getValue().upper);
        // }

        // loop to compress using the rangesMap!
        for (int i = 0; i < rawData.length(); i++) {

            double range = globalUpper-globalLower;
            double oldLower = globalLower;
            
            globalLower = globalLower + range * rangesMap.get(rawData.charAt(i)).lower;
            globalUpper = oldLower + range * rangesMap.get(rawData.charAt(i)).upper;

        }
        // System.out.println(globalLower + "   " + globalUpper);

        writeProbMap(probMap);

        try {

            FileWriter out = new FileWriter("rawDataSize.txt");
            out.write(Integer.toString(rawData.length()));
            out.close();

        } catch (IOException e) {
            System.out.println("didnt find file");
            
        }
        

        return (globalLower+globalUpper)/2;
    }

    public static String decompress(double doubleDec) {

        Map<Character, CharEntry> rangesMap = readProbMap();
        String decompressedData = "";

        double globalLower = 0.0;
        double globalUpper = 1.0;
        double searchDoubleDec = doubleDec;

        try {

            Scanner lengthScan = new Scanner(new File("rawDataSize.txt"));
            int length = Integer.parseInt(lengthScan.nextLine());
            lengthScan.close();

            for (int i = 0; i < length; i++) {
                for (Map.Entry<Character, CharEntry> entry : rangesMap.entrySet()) {
                    
                    // calc new code
                    // calc new lower and upper
                    // search for char with this code inside and append on the decompressedData

                    if (searchDoubleDec > entry.getValue().lower && searchDoubleDec < entry.getValue().upper) {

                        decompressedData += entry.getKey();

                        double range = globalUpper-globalLower;
                        double oldLower = globalLower;
                        globalLower = globalLower + range * entry.getValue().lower;
                        globalUpper = oldLower + range * entry.getValue().upper;

                        searchDoubleDec = (doubleDec - globalLower)/(globalUpper - globalLower);

                        break;
                    

                    }

                }

            }

            try {

                decompressedData = decompressedData.replace('$', '\n');
                FileWriter out = new FileWriter("output.txt");
                out.write(decompressedData);
                out.close();
    
            } catch (IOException e) {
                System.out.println("didnt find file");
                
            }

            // System.out.println(decompressedData);

        } catch (FileNotFoundException e) {
            System.out.println("didnt find file");
            
        }
        
        return decompressedData;
    }

    public static void main(String[] args) {

        System.out.println("Welcome to Arthmitice floating-point compression: ");
        System.out.println("please select: ");
        System.out.println("");
        System.out.println("1. compress");
        System.out.println("2. decompress");

        Scanner scanner = new Scanner(System.in);
        int option = Integer.parseInt(scanner.nextLine());

        if(option == 1) {
            System.out.println("Please enter input file name to compress: ");

            String inputFileName = scanner.nextLine();
            String inputToCompress = readInput(inputFileName);

            double compressesDouble = compress(inputToCompress);

            String compressesBin = turnToBinary(compressesDouble);

            System.out.println("Save compressed bits in: ");
            String outputFileName = scanner.nextLine();

            writeToBinFile(compressesBin, outputFileName);

            // System.out.println(compressesDouble);

        } else if (option == 2) {
            System.out.println("Please enter input file name to decompress: ");
            
            String inputFileName = scanner.nextLine();
            
            String bin = readFromBinFile(inputFileName);
            
            double doubleToDecompressFrom = turnToDecimal(bin);
            
            String decompressesData = decompress(doubleToDecompressFrom);
            
            
        } else {
            System.out.println("Undefined input");

        }
    }
}
