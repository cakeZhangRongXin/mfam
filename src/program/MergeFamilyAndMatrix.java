package program;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.util.LinkedList;
import java.io.FileWriter;
public class MergeFamilyAndMatrix {
	//The path of GSE*_family file
	private String familyFilePath;
	//The path of GSE*_series_matrix file
	private String matrixFilePath;
	//The column of gene Symbol
	private int geneCol;
	//store the mapping of probe to gene symbol
	private HashMap<String,String> probeToGene = new HashMap<>();
	//store the matrix 
	private String[][] matrix;
	//The row of matrix
	private int matrixRow;
	//The header of matrix
	private String header;
	//store the mismatched probe
	private LinkedList<String> unMatchedProbe = new LinkedList<>();
	//the output file path
	private String outputFilePath;
	
	public MergeFamilyAndMatrix() {
		geneCol = 10;
		matrixRow = 0;
	}
	
	private void setSoftFilePath(String familyFilePath) {
		this.familyFilePath = familyFilePath;
	}
	
	private String getSoftFilePath() {
		return familyFilePath;
	}
	
	private void setMatrixFilePath(String matrixFilePath) {
		this.matrixFilePath = matrixFilePath;
	}
	
	private String getMatrixFilePath() {
		return matrixFilePath;
	} 
	
	private void setGeneCol(int geneCol) {
		this.geneCol = geneCol; 
	}
	
	private int getGeneCol() {
		return geneCol;
	}
	
	private HashMap<String,String> getMap() {
		return probeToGene;
	} 
	
	private void setMap() {
		boolean flag = false;
		String line = "";
		String[] temp;
		try{
			Scanner scan = new Scanner(new File(familyFilePath), "UTF-8"); 
			//read the file until meet the platform line 
			line = scan.nextLine();
			while(!flag) {
				if (line.equalsIgnoreCase("!platform_table_begin")) {
					flag = true;
				} else {
					line = scan.nextLine();
				}								
			}
			//skip the attributes line
			scan.nextLine();
			//start to map 
			while(flag) {
				line = scan.nextLine().trim();
				if (!line.equalsIgnoreCase("!platform_table_end")) {
					temp = line.split("\t");
					//System.out.println(temp.length);
					if (temp.length > geneCol) {
						probeToGene.put(temp[0], temp[geneCol]);
					} else {
						System.out.println("warning,maybe the line do not has gene symbol line!  probe: "+temp[0]);
					}
					temp = null;
				} else {
					flag = false;
				}
							
			}

			scan.close();
		} catch(Exception e) {
			System.out.println("Problem occurs when read the family.soft file    :(\n");
			e.getMessage();
			System.exit(0);
		}
		
	}
	
	private int getMatrixRow() {
		return matrixRow;
	}
	
	private void setMatrixRow() {
		boolean flag = false;
		String line;
	
		try{
			Scanner scan = new Scanner(new File(matrixFilePath), "UTF-8"); 
			//read the file until meet the platform line
		
			while(scan.hasNextLine()) {
				line = scan.nextLine().trim();
				
				if (line.equalsIgnoreCase("!series_matrix_table_end")) {
					flag = false;
				}

				if (line.equalsIgnoreCase("!series_matrix_table_begin")) {
					flag = true;
				}
				
				if (flag) {
					matrixRow++;
				}
			}
			
			scan.close();
		} catch(Exception e) {
			System.out.println("Problem occurs when read the matrix file    :(\n");
			e.getMessage();
			e.printStackTrace();
			System.exit(0);
		}
		matrixRow = matrixRow - 2;
	}
	
	public String[][] getMatrix() {
		return matrix;
	}
	
	public void setMatrix() {
		matrix = new String[matrixRow][];
		boolean flag = false;
		String line;
		int row = 0;
		String[] temp;
		try{
			Scanner scan = new Scanner(new File(matrixFilePath), "UTF-8"); 
			//read the file until meet the platform line 
			while(scan.hasNextLine()) {
				line = scan.nextLine().trim();
				
				if (line.equalsIgnoreCase("!series_matrix_table_end")) {
					flag = false;
				}
				
				//put the data into matrix
				if (flag) {
					temp = line.split("\t");	
					temp[0] = temp[0].trim().replace("\"", "");
					if (probeToGene.containsKey(temp[0])) {
						temp[0] = probeToGene.get(temp[0]);
						matrix[row++] = temp;
					} else {
						unMatchedProbe.add(temp[0]);
					}
					
					temp = null;
				}
				
				if (line.equalsIgnoreCase("!series_matrix_table_begin")) {
					flag = true;
					header = scan.nextLine();
					header = header.replace("\"", "");
				}
						
			}

			scan.close();
		} catch(Exception e) {
			System.out.println("Problem occurs when read the matrix file    :(\n");
			e.getMessage();
			System.exit(0);
		}
		
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}
	
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
	
	public void writeFile() {
		try{
			//to  write the matched matrix
			FileWriter out1 = new FileWriter(outputFilePath, true);
			//to write the mismatched probe
			FileWriter out2 = new FileWriter(outputFilePath+".unmapped.txt", true);
			
			//write the matrix header
			out1.write(header);
			out1.write("\n");
			//write the matrix content
			for(int i = 0; i < matrix.length; i++) {
				if (matrix[i] != null) {
					for(int j = 0; j < matrix[i].length; j++) {
						out1.write(matrix[i][j]);
						if (j != matrix[i].length -1) {
							out1.write("\t");
						}
					}
					out1.write("\n");
				}
			}
			
			//write the mismatched file
			for(String temp : unMatchedProbe) {
				out2.write(temp);
				out2.write("\n");
			}
			
			out1.close();
			out2.close();
		} catch(Exception e) {
			System.out.println("Problem occurs when write the result file    :(\n");
			e.getMessage();
			System.exit(0);
		}
	} 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MergeFamilyAndMatrix  mfam = new MergeFamilyAndMatrix();
		
		System.out.println("Welcome!  :)");
		if (args.length == 1 && args[0].equalsIgnoreCase("-h")) {
			System.out.println("example:");
			System.out.println("run like this: ");
			System.out.println("java -jar mfam.jar ./GSE19332_family.soft ./GSE19332_series_matrix.txt 10 ./GSE19332_result.txt");
			System.out.println("explanation: ./GSE19332_family.soft is your soft file path, GSE19332_series_matrix.txt is your matrix file path,"
					+ "10 is the column of gene symbol in your soft file, ./GSE19332_result.txt is the result file path");
		}
		if (args.length == 4) {
			System.out.println("start");
			mfam.setSoftFilePath("\""+args[0]+"\"");
			mfam.setMatrixFilePath("\""+args[1]+"\"");
			mfam.setGeneCol(Integer.parseInt(args[2])-1);
			mfam.setOutputFilePath("\""+args[3]+"\"");
			System.out.println("start to read the "+args[0]);
			mfam.setMap();
			System.out.println(args[0]+"read over");
			System.out.println("start to calculate the row of matrix ");
			mfam.setMatrixRow();
			System.out.println("calculate over");
			System.out.println("start to read the "+args[1]);
			mfam.setMatrix();
			System.out.println(args[1]+"read over");
			System.out.println("start to write the result file");
			mfam.writeFile();	
			System.out.println("end!");
			System.out.println("=====================Thank  you=====================");
		} else {
			System.out.println("The number of parameters is incorrect    :(");
		}
	}

}
