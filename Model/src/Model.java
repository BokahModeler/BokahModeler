import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Model {
	ArrayList<ArrayList<float[]>> nose  =new ArrayList<ArrayList<float[]>>();
	public Model(){
		createNose();
		//close scanner
	}
	public void createNose(){
		float[] vectors;
		ArrayList<float[]> polygon = new ArrayList<float[]>();
		try{
			File file = new File("models/NoseCoordinatesEdited.txt");
			Scanner scan = new Scanner(file);
			String[] temp;
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(!line.startsWith("v") && !line.isEmpty()){
					temp = line.split(" ");
					vectors = new float[]{Float.parseFloat(temp[0]),Float.parseFloat(temp[1]),Float.parseFloat(temp[2])};
					polygon.add(vectors);
				}
				if(polygon.size()==4){
					nose.add(polygon);
					polygon = new ArrayList<float[]>();
				}
			}	
			scan.close();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
	public ArrayList<ArrayList<float[]>> getNose(){
		return nose;
	}
	/*public static void main(String[] args){
		Model mod = new Model();
		ArrayList<ArrayList<float[]>>nose =mod.getNose();
		System.out.println(nose.size());
	}*/
}
